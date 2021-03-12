package com.github.zhanghui.integration.core.process.pre.registrar.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zhanghui.annotation.Caller;
import com.github.zhanghui.annotation.Supplier;
import com.github.zhanghui.exception.PluginContextRuntimeException;
import com.github.zhanghui.exception.PluginInvokeException;
import com.github.zhanghui.integration.core.process.pre.classes.group.CallerGroup;
import com.github.zhanghui.integration.core.process.pre.classes.group.SupplierGroup;
import com.github.zhanghui.integration.core.process.pre.registrar.AbstractPluginBeanRegistrar;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * 提供插件中负责插件通信的bean的注册（被@Supplier和@Caller注释的类）
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/9 13:52
 **/
@Slf4j
public class InvokeBeanRegistrar extends AbstractPluginBeanRegistrar {
    /**
     * 存放所有插件的@Supplier暴露的bean
     */
    private final static Map<String, Map<String, Object>> PLUGIN_SUPPLIER_MAP = new ConcurrentHashMap<>(4);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void registry(PluginRegistryInfo pluginRegistryInfo) throws Exception {
        registrySupplierBeans(pluginRegistryInfo);
        registryCallerBeans(pluginRegistryInfo);
    }

    @Override
    public void unRegistry(PluginRegistryInfo pluginRegistryInfo) throws Exception {
        PLUGIN_SUPPLIER_MAP.remove(pluginRegistryInfo.getPluginWrapper().getPluginId());
    }

    /**
     * 注册服务提供者相关bean
     */
    private void registrySupplierBeans(PluginRegistryInfo pluginRegistryInfo) {
        final String pluginId = pluginRegistryInfo.getPluginWrapper().getPluginId();

        List<Class> supplierClasses = pluginRegistryInfo.getClassesFromGroup(SupplierGroup.GROUP_ID);
        if (supplierClasses == null || supplierClasses.isEmpty()) {
            return;
        }
        for (Class<?> supplierClass : supplierClasses) {

            Supplier supplier = supplierClass.getAnnotation(Supplier.class);
            if (supplier == null) {
                continue;
            }
            String beanName = supplier.value();
            if (super.existBean(pluginRegistryInfo,beanName)) {
                log.error("插件 [{}] 中bean [{}] 已经存在，注册失败", pluginId, beanName);
                throw new PluginContextRuntimeException("插件 [" + pluginRegistryInfo.getPluginWrapper().getPluginId() + "] 中bean [" + beanName + "] 已经存在，注册失败");
            }
            register(beanName, pluginRegistryInfo, supplierClass);
            // 注册到单个插件信息中
            pluginRegistryInfo.addSupplierBean(beanName);
        }
    }

    /**
     * 注册服务消费者相关beans
     */
    private void registryCallerBeans(PluginRegistryInfo pluginRegistryInfo) {
        List<Class> callerClasses = pluginRegistryInfo.getClassesFromGroup(CallerGroup.GROUP_ID);
        if(callerClasses == null || callerClasses.isEmpty()){
            return;
        }

        for(Class<?> callerClass : callerClasses){

            Caller callerAnnotation = callerClass.getAnnotation(Caller.class);
            if(callerAnnotation == null){
                continue;
            }

            super.register(pluginRegistryInfo,callerClass,(beanDefinition ->{
                beanDefinition.getPropertyValues().add("callerInterface",callerClass);
                beanDefinition.getPropertyValues().add("callerAnnotation",callerAnnotation);
                beanDefinition.setBeanClass(PluginCallerFactoryBean.class);
                beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
            }));
        }

    }

    /**
     * 根据callInterface接口信息和callerAnnotation注解信息生产被Caller注解接口的代理bean
     * 并注册到插件的spring上下文中
     *
     * @param <T>
     */
    private static class PluginCallerFactoryBean<T> implements FactoryBean<T> {

        private  Class<T> callerInterface;
        private  Caller callerAnnotation;

        @Override
        public T getObject() throws Exception {
            ClassLoader classLoader = callerInterface.getClassLoader();
            Class<?>[] interfaces = new Class[]{callerInterface};
            InvocationHandler invocationHandler = new CallerInvokeHandler(callerAnnotation);

            return (T) Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);
        }

        @Override
        public Class<?> getObjectType() {
            return callerInterface;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        public Class<T> getCallerInterface() {
            return callerInterface;
        }

        public void setCallerInterface(Class<T> callerInterface) {
            this.callerInterface = callerInterface;
        }

        public Caller getCallerAnnotation() {
            return callerAnnotation;
        }

        public void setCallerAnnotation(Caller callerAnnotation) {
            this.callerAnnotation = callerAnnotation;
        }
    }

    /**
     * 被Caller注解接口的代理bean 的方法拦截调用逻辑
     */
    private static class CallerInvokeHandler implements InvocationHandler {

        private final Caller callerAnnotation;

        public CallerInvokeHandler(Caller callerAnnotation) {
            this.callerAnnotation = callerAnnotation;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String pluginId = callerAnnotation.pluginId();
            String callerServiceName = callerAnnotation.value();

            Object supplierBean;

            if (StringUtils.isNotNull(pluginId)) {
                supplierBean = getSupplierManifested(pluginId, callerServiceName);
                if (supplierBean == null) {
                    throw new PluginInvokeException("找不到任何插件可以提供服务 [" + callerServiceName + "]");
                }
            } else {
                supplierBean = getSupplierDefault(callerServiceName);
                if (supplierBean == null) {
                    throw new PluginInvokeException("在插件 [" + pluginId + "] 中没有发现服务 [" + callerServiceName + "]");
                }
            }
            Caller.Method callerMethod = method.getAnnotation(Caller.Method.class);

            if (callerMethod == null) {
                return getInvokeResultDefault(method, supplierBean, args);
            } else {
                return getInvokeResultManifested(method, callerMethod, supplierBean, args);
            }
        }
    }

    /**
     * 不指定pluginId，从所有的插件的Supplier Bean中获取
     *
     * @param name
     * @return
     */
    private static Object getSupplierDefault(String name) {
        for (Map<String, Object> superMap : PLUGIN_SUPPLIER_MAP.values()) {
            Object o = superMap.get(name);
            if (o != null) {
                return o;
            }
        }
        return null;
    }

    /**
     * 指定pluginId，从指定的插件的Supplier Bean中获取
     *
     * @param pluginId
     * @param name
     * @return
     */
    private static Object getSupplierManifested(String pluginId, String name) {
        Map<String, Object> supplierMap = PLUGIN_SUPPLIER_MAP.get(pluginId);
        if (supplierMap == null || supplierMap.isEmpty()) {
            return null;
        }
        return supplierMap.get(name);
    }

    /**
     * 指定pluginId，添加指定的插件Supplier Bean
     *
     * @param pluginId
     * @param name
     * @param supplierBean
     */
    public static void addSupplierManifested(String pluginId, String name, Object supplierBean){
        Map<String, Object> supplierMap = PLUGIN_SUPPLIER_MAP.computeIfAbsent(pluginId,k-> new HashMap<>());
        supplierMap.put(name, supplierBean);
    }

    /**
     * 在Supplier提供的服务中遍历所有Method进行匹配
     * 对成功匹配到的Method进行调用
     *
     * @return 成功匹配的情况下返回匹配到的Method调用的结果 匹配失败的情况下抛出异常
     */
    private static Object getInvokeResultDefault(Method method, Object supplierBean, Object[] args) throws Exception {
        Class supplierBeanClass = supplierBean.getClass();
        Class[] argsClasses = null;
        if(args != null) {
            argsClasses = new Class[args.length];

            for (int i = 0; i < args.length; i++) {
                argsClasses[i] = args[i].getClass();
            }
        }
        String methodName = method.getName();
        Method invokeMethod = supplierBeanClass.getMethod(methodName, argsClasses);
        if (invokeMethod == null) {
            throw new PluginInvokeException("在 supplierBean [" + supplierBeanClass.getName() + "] 中找不到匹配的方法 [" + methodName + "]");
        }
        Object invokeResult = invokeMethod.invoke(supplierBean, args);
        return wrapInvokeResult(invokeResult, method);
    }

    /**
     * 在Supplier提供的服务指定的Method进行匹配，包括入参和返回结果
     * 对成功匹配到的Method进行调用
     *
     * @return 成功匹配的情况下返回匹配到的Method调用的结果 匹配失败的情况下抛出异常
     */
    private static Object getInvokeResultManifested(Method method, Caller.Method callerMethod, Object supplierBean, Object[] args) throws Exception {

        String callerMethodName = callerMethod.value();
        Class supplierBeanClass = supplierBean.getClass();
        Method[] supplierMethods = supplierBeanClass.getMethods();

        Method targetMethod = null;

        for (Method supplierMethod : supplierMethods) {

            if (Objects.equals(callerMethodName, supplierMethod.getName())) {
                targetMethod = supplierMethod;
                break;
            }
        }
        // 找不到指定的Caller.Method配置的方法名， 走默认处理情况（遍历supplier Bean下所有的Method）
        if (targetMethod == null) {
            return getInvokeResultDefault(method, supplierBean, args);
        }

        Class<?>[] targetMethodParameterTypes = targetMethod.getParameterTypes();
        // 调用参数个数不匹配，走默认处理情况（遍历supplier Bean下所有的Method）
        if (targetMethodParameterTypes.length != args.length) {
            return getInvokeResultDefault(method, supplierBean, args);
        }
        Object[] invokeArgs = null;
        if(args != null) {
            invokeArgs = new Object[args.length];
            for (int i = 0; i < targetMethodParameterTypes.length; i++) {
                Class<?> targetMethdParameterType = targetMethodParameterTypes[i];

                if (ClassUtils.isAssignable(targetMethdParameterType, args[i].getClass())) {
                    invokeArgs[i] = args[i];
                } else {
                    // 尝试将参数类型和值转换成目标可执行方法的参数类型和值
                    String parseData = OBJECT_MAPPER.writeValueAsString(args[i]);
                    invokeArgs[i] = OBJECT_MAPPER.readValue(parseData, targetMethdParameterType);
                }
            }
        }
        Object invokeResult = targetMethod.invoke(supplierBean, invokeArgs);
        return wrapInvokeResult(invokeResult, method);
    }

    /**
     * 对invokeResult 进行校验和转换，在返回值不匹配的情况下使用序列化尝试转换成指定返回类型
     *
     * @param invokeResult
     * @param method
     * @return
     */
    private static Object wrapInvokeResult(Object invokeResult, Method method) throws Exception {
        if (invokeResult == null) {
            return invokeResult;
        }
        Class invokeResultClass = invokeResult.getClass();
        // 返回值类型匹配的情况下
        if (ClassUtils.isAssignable(invokeResultClass, method.getReturnType())) {
            return invokeResult;
        } else {
            String parseData = OBJECT_MAPPER.writeValueAsString(invokeResult);
            return OBJECT_MAPPER.readValue(parseData, OBJECT_MAPPER.getTypeFactory().constructType(method.getGenericReturnType()));
        }
    }
}


