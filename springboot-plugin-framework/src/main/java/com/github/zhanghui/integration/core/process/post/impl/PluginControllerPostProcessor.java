package com.github.zhanghui.integration.core.process.post.impl;

import com.github.zhanghui.integration.configuration.IntegrationConfiguration;
import com.github.zhanghui.integration.core.process.post.PluginPostProcessor;
import com.github.zhanghui.integration.core.process.post.extension.ControllerWrapper;
import com.github.zhanghui.integration.core.process.post.extension.PluginControllerExtension;
import com.github.zhanghui.integration.core.process.pre.classes.group.ControllerGroup;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.utils.AopUtils;
import com.github.zhanghui.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/20 9:57
 **/
@Slf4j
public class PluginControllerPostProcessor implements PluginPostProcessor {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final IntegrationConfiguration configuration;
    private List<PluginControllerExtension> pluginControllerExtensions;

    public PluginControllerPostProcessor(ApplicationContext mainApplicationContext) {
        this.requestMappingHandlerMapping = mainApplicationContext.getBean(RequestMappingHandlerMapping.class);
        this.configuration = mainApplicationContext.getBean(IntegrationConfiguration.class);
        pluginControllerExtensions = getPluginControllerExtensions(mainApplicationContext);
    }

    @Override
    public void initialize() throws Exception {
        resolveControllerExtension(PluginControllerExtension::initialize);
    }

    @Override
    public void registry(List<PluginRegistryInfo> pluginRegistryInfos) throws Exception {
        pluginRegistryInfos.forEach(pluginRegistryInfo -> {
            AopUtils.resolveAop(pluginRegistryInfo);

            try {
                List<Class> groupClasses = pluginRegistryInfo.getClassesFromGroup(ControllerGroup.GROUP_ID);
                if (groupClasses == null || groupClasses.isEmpty()) {
                    return;
                }
                String pluginId = pluginRegistryInfo.getPluginWrapper().getPluginId();
                List<ControllerWrapper> controllerWrapperList = Lists.newArrayList();
                for (Class controllerClass : groupClasses) {
                    if (controllerClass == null) {
                        continue;
                    }
                    // 注册当前Controller类到主版本的mvc环境中
                    ControllerWrapper controllerWrapper = doRegistry(pluginRegistryInfo, controllerClass);
                    if (controllerWrapper != null) {
                        controllerWrapperList.add(controllerWrapper);
                    }
                }
                pluginRegistryInfo.addControllerWrappers(controllerWrapperList);

                resolveControllerExtension(pluginControllerExtension -> {
                    try {
                        pluginControllerExtension.registry(pluginId, controllerWrapperList);
                    } catch (Exception e) {
                        log.error("扩展插件Controller 处理类 [{}] 处理 插件 [{}] 出错：[{}]", pluginControllerExtension.getClass().getName(), pluginId, e.getMessage(), e);
                    }
                });
            } finally {
                AopUtils.recoverAop();
            }
        });
    }

    @Override
    public void unRegistry(List<PluginRegistryInfo> pluginRegistryInfos) throws Exception {
        for(PluginRegistryInfo pluginRegistryInfo : pluginRegistryInfos){
            List<ControllerWrapper> controllerWrapperList = pluginRegistryInfo.getPluginControllerWrappers();
            if(controllerWrapperList == null || controllerWrapperList.size() == 0){
                continue;
            }

            for(ControllerWrapper controllerWrapper : controllerWrapperList){
                if(controllerWrapper == null){
                    continue;
                }
                // 从主版本的mvc环境中去除目标Controller
                doUnRegistry(controllerWrapper);
            }
            String pluginId = pluginRegistryInfo.getPluginWrapper().getPluginId();
            resolveControllerExtension(pluginControllerExtension -> {
                try {
                    pluginControllerExtension.unRegistry(pluginId, controllerWrapperList);
                }catch (Exception e){
                    log.error("插件扩展Controller [{}] 卸载 插件 [{}] 时出现异常: [{}]",pluginControllerExtension.getClass().getName(),pluginId,e.getMessage(),e);
                }
            });
        }
    }

    /**
     * 获取指定上下文中的Controller的扩展类
     *
     * @param applicationContext 指定上下文
     * @return
     */
    private List<PluginControllerExtension> getPluginControllerExtensions(ApplicationContext applicationContext) {
        Map<String, PluginControllerExtension> beansOfType = applicationContext.getBeansOfType(PluginControllerExtension.class);
        if (beansOfType == null || beansOfType.isEmpty()) {
            return null;
        }
        return new ArrayList<>(beansOfType.values());
    }

    /**
     * 对pluginControllerExtensions 定义操作行为
     *
     * @param controllerExtensionConsumer 遍历时的操作行为
     */
    private void resolveControllerExtension(Consumer<PluginControllerExtension> controllerExtensionConsumer) {
        if (pluginControllerExtensions == null || pluginControllerExtensions.isEmpty()) {
            return;
        }
        pluginControllerExtensions.forEach(controllerExtensionConsumer);
    }

    /**
     * 注册当前Controller类到主版本的mvc环境中
     *
     * @param pluginRegistryInfo
     * @param controllerClass
     * @return
     */
    private ControllerWrapper doRegistry(PluginRegistryInfo pluginRegistryInfo, Class<?> controllerClass) {
        String pluginId = pluginRegistryInfo.getPluginWrapper().getPluginId();
        GenericApplicationContext pluginApplicationContext = pluginRegistryInfo.getPluginApplicationContext();

        Object controllerBean = pluginApplicationContext.getBean(controllerClass);
        ControllerWrapper controllerWrapper = new ControllerWrapper();
        controllerWrapper.setBeanClass(controllerClass);
        Set<RequestMappingInfo> requestMappingInfos = Sets.newHashSet();

        try {
            //修改controllerClass中RequestMapping映射路径
            setPathPrefix(pluginId, controllerClass);

            Method getMappingForMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod",Method.class,Class.class);
            getMappingForMethod.setAccessible(true);

            // 遍历controllerClass所有Method 过滤出被RequestMapping注释的方法 然后注册到主版本的RequestMapping上下文中去
            Method[] targetMethods = controllerClass.getMethods();
            for (Method targetMethod : targetMethods) {
                if (isHaveRequestMapping(targetMethod)) {
                    RequestMappingInfo requestMappingInfo = (RequestMappingInfo) getMappingForMethod.invoke(requestMappingHandlerMapping, targetMethod, controllerClass);
                    requestMappingHandlerMapping.registerMapping(requestMappingInfo, controllerBean, targetMethod);
                    requestMappingInfos.add(requestMappingInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        controllerWrapper.setRequestMappingInfos(requestMappingInfos);
        return controllerWrapper;
    }

    /**
     * 从主版本的mvc环境中去除Controller
     */
    private void doUnRegistry(ControllerWrapper controllerWrapper){
        Set<RequestMappingInfo> requestMappingInfos = controllerWrapper.getRequestMappingInfos();
        if(requestMappingInfos != null && requestMappingInfos.size() > 0) {
            requestMappingInfos.forEach(requestMappingHandlerMapping::unregisterMapping);
        }
    }
    /**
     * 修改controllerClass中RequestMapping映射路径
     * 如果在启用插件id做前缀的情况需要加上插件id前缀
     * 如果自定义了访问前缀需要加上自定义前缀
     *
     * @param pluginId
     * @param controllerClass
     */
    private void setPathPrefix(String pluginId, Class<?> controllerClass) throws NoSuchFieldException, IllegalAccessException {
        RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
        if (requestMapping == null) {
            return;
        }

        String pathPrefix = configuration.pluginRestPathPrefix();
        if (configuration.enablePluginIdRestPathPrefix()) {
            if (StringUtils.isNotNull(pathPrefix)) {
                pathPrefix = assemblePluginPathPrefix(pathPrefix, pluginId);
            } else {
                pathPrefix = pluginId;
            }
        } else if (StringUtils.isNull(pathPrefix)) {
            return;
        }

        //生成 RequestMapping 注解的代理类
        InvocationHandler annotationInvocationHandler = Proxy.getInvocationHandler(requestMapping);
        // 修改代理类里的memberValues变量
        Field memberValuesField = annotationInvocationHandler.getClass().getDeclaredField("memberValues");
        memberValuesField.setAccessible(true);

        Map<String, Object> memberValues = (Map<String, Object>) memberValuesField.get(annotationInvocationHandler);

        Set<String> customPaths = Sets.newHashSet();
        customPaths.addAll(Arrays.asList(requestMapping.value()));
        customPaths.addAll(Arrays.asList(requestMapping.path()));

        String[] newRequestMappingValues = new String[customPaths.size()];
        int i = 0;

        for(String customPath : customPaths){
            // 避免出现重复的前缀
            if(customPath.contains(pathPrefix)){
                newRequestMappingValues[i++] = customPath;
            }else{
                newRequestMappingValues[i++] = assemblePluginPathPrefix(pathPrefix,customPath);
            }
        }

        if(newRequestMappingValues.length == 0){
            newRequestMappingValues = new String[]{pathPrefix};
        }
        // 最终成功替换memberValues中的path和value变量
        memberValues.put("path",newRequestMappingValues);
        // 覆盖之前的value
        memberValues.put("value",new String[]{});
    }

    /**
     * 判断method有没有被RequestMapping/PostMapping/GetMapping注解
     *
     * @param targetMethod
     * @return
     */
    private boolean isHaveRequestMapping(Method targetMethod) {
        return AnnotationUtils.getAnnotation(targetMethod, RequestMapping.class) != null;
    }

    /**
     * 将两个路径拼接成一个可访问的路径 （var1/var2）
     *
     * @param var1
     * @param var2
     * @return
     */
    private String assemblePluginPathPrefix(String var1, String var2) {
        if (StringUtils.isNull(var1)) {
            return var2;
        } else if (StringUtils.isNull(var2)) {
            return var1;
        } else {
            if (var1.endsWith("/") && var2.startsWith("/")) {
                return var1.concat(var2.substring(1));
            } else if (!var1.endsWith("/") && !var2.startsWith("/")) {
                return var1.concat("/").concat(var2);
            } else {
                return var1.concat(var2);
            }
        }
    }

}
