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
                    // ????????????Controller??????????????????mvc?????????
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
                        log.error("????????????Controller ????????? [{}] ?????? ?????? [{}] ?????????[{}]", pluginControllerExtension.getClass().getName(), pluginId, e.getMessage(), e);
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
                // ???????????????mvc?????????????????????Controller
                doUnRegistry(controllerWrapper);
            }
            String pluginId = pluginRegistryInfo.getPluginWrapper().getPluginId();
            resolveControllerExtension(pluginControllerExtension -> {
                try {
                    pluginControllerExtension.unRegistry(pluginId, controllerWrapperList);
                }catch (Exception e){
                    log.error("????????????Controller [{}] ?????? ?????? [{}] ???????????????: [{}]",pluginControllerExtension.getClass().getName(),pluginId,e.getMessage(),e);
                }
            });
        }
    }

    /**
     * ???????????????????????????Controller????????????
     *
     * @param applicationContext ???????????????
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
     * ???pluginControllerExtensions ??????????????????
     *
     * @param controllerExtensionConsumer ????????????????????????
     */
    private void resolveControllerExtension(Consumer<PluginControllerExtension> controllerExtensionConsumer) {
        if (pluginControllerExtensions == null || pluginControllerExtensions.isEmpty()) {
            return;
        }
        pluginControllerExtensions.forEach(controllerExtensionConsumer);
    }

    /**
     * ????????????Controller??????????????????mvc?????????
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
            //??????controllerClass???RequestMapping????????????
            setPathPrefix(pluginId, controllerClass);

            Method getMappingForMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod",Method.class,Class.class);
            getMappingForMethod.setAccessible(true);

            // ??????controllerClass??????Method ????????????RequestMapping??????????????? ???????????????????????????RequestMapping???????????????
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
     * ???????????????mvc???????????????Controller
     */
    private void doUnRegistry(ControllerWrapper controllerWrapper){
        Set<RequestMappingInfo> requestMappingInfos = controllerWrapper.getRequestMappingInfos();
        if(requestMappingInfos != null && requestMappingInfos.size() > 0) {
            requestMappingInfos.forEach(requestMappingHandlerMapping::unregisterMapping);
        }
    }
    /**
     * ??????controllerClass???RequestMapping????????????
     * ?????????????????????id????????????????????????????????????id??????
     * ?????????????????????????????????????????????????????????
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

        //?????? RequestMapping ??????????????????
        InvocationHandler annotationInvocationHandler = Proxy.getInvocationHandler(requestMapping);
        // ?????????????????????memberValues??????
        Field memberValuesField = annotationInvocationHandler.getClass().getDeclaredField("memberValues");
        memberValuesField.setAccessible(true);

        Map<String, Object> memberValues = (Map<String, Object>) memberValuesField.get(annotationInvocationHandler);

        Set<String> customPaths = Sets.newHashSet();
        customPaths.addAll(Arrays.asList(requestMapping.value()));
        customPaths.addAll(Arrays.asList(requestMapping.path()));

        String[] newRequestMappingValues = new String[customPaths.size()];
        int i = 0;

        for(String customPath : customPaths){
            // ???????????????????????????
            if(customPath.contains(pathPrefix)){
                newRequestMappingValues[i++] = customPath;
            }else{
                newRequestMappingValues[i++] = assemblePluginPathPrefix(pathPrefix,customPath);
            }
        }

        if(newRequestMappingValues.length == 0){
            newRequestMappingValues = new String[]{pathPrefix};
        }
        // ??????????????????memberValues??????path???value??????
        memberValues.put("path",newRequestMappingValues);
        // ???????????????value
        memberValues.put("value",new String[]{});
    }

    /**
     * ??????method????????????RequestMapping/PostMapping/GetMapping??????
     *
     * @param targetMethod
     * @return
     */
    private boolean isHaveRequestMapping(Method targetMethod) {
        return AnnotationUtils.getAnnotation(targetMethod, RequestMapping.class) != null;
    }

    /**
     * ???????????????????????????????????????????????? ???var1/var2???
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
