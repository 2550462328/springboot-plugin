package com.github.zhanghui.resolver;

import com.github.zhanghui.PluginResource;
import com.github.zhanghui.SpringBootStaticResourceConfig;
import com.github.zhanghui.exception.PluginExtensionRuntimeException;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.realize.BasePlugin;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * 静态资源解析处理类
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/5 17:30
 **/
@Slf4j
public class PluginResourceResolver implements ResourceResolver {

    /**
     * 插件扩展的静态资源集合
     */
    private final static Map<String, PluginStaticResource> PLUGIN_STATIC_RESOURCE_MAP = new ConcurrentHashMap<>();

    private final static String PLUGIN_RESOURCE_CACHE_KEY_PREFIX = "staticResourceCache-";

    @Override
    public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {

        int startIndex = requestPath.startsWith("/") ? 1 : 0;
        int endIndex = requestPath.indexOf("/", 1);

        if (endIndex != -1) {
            String pluginId = requestPath.substring(startIndex, endIndex);
            String partialPath = requestPath.substring(endIndex + 1);

            PluginStaticResource pluginStaticResource = PLUGIN_STATIC_RESOURCE_MAP.get(pluginId);
            if (pluginStaticResource == null) {
                return null;
            }

            // 跟据pluginId计算缓存key
            String cacheKey = computeKey(pluginId);
            // 有缓存直接取缓存
            if (pluginStaticResource.hasCacheResource(pluginId)) {
                return pluginStaticResource.getCacheResource(pluginId);
            }

            Resource resource;

            // 基于classpath路径解析出插件Resource
            resource = resolveResourceFromClassPath(pluginStaticResource,partialPath);
            if(resource != null){
                pluginStaticResource.addCacheResourceIfAbsent(cacheKey,resource);
                return resource;
            }

            // 基于File文件路径加载插件Resource
            resource = resolveResourceFromFilePath(pluginStaticResource,partialPath);
            if(resource != null){
                pluginStaticResource.addCacheResourceIfAbsent(cacheKey,resource);
                return resource;
            }

            return null;
        }
        return chain.resolveResource(request, requestPath, locations);
    }

    @Override
    public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
        return null;
    }

    /**
     * 计算资源缓存中的插件扩展的key值
     *
     * @param pluginId
     * @return
     */
    private String computeKey(String pluginId) {
        return PLUGIN_RESOURCE_CACHE_KEY_PREFIX.concat(pluginId);
    }

    /**
     * 基于classpath路径解析出插件Resource
     *
     * @param pluginStaticResource
     * @param partialPath
     * @return
     */
    private Resource resolveResourceFromClassPath(PluginStaticResource pluginStaticResource, String partialPath){

        Set<String> classPaths = pluginStaticResource.getClassPaths();
        if(classPaths == null || classPaths.isEmpty()){
            return null;
        }

        PluginRegistryInfo pluginRegistryInfo = pluginStaticResource.getPluginRegistryInfo();
        BasePlugin basePlugin = pluginRegistryInfo.getBasePlugin();
        if(basePlugin == null){
            return null;
        }

//        ClassLoader pluginClassLoader = pluginRegistryInfo.getPluginClassLoader(PluginRegistryInfo.ClassLoaderStrategy.PDA);
        for(String classPath : classPaths){
            PluginResource pluginResource = new PluginResource(classPath + partialPath,pluginRegistryInfo);
//            pluginResource.setClassLoader(pluginClassLoader);
            if(pluginResource.exists()){
                return pluginResource;
            }
        }
        return null;
    }

    /**
     * 基于File文件路径加载插件Resource
     *
     * @param pluginStaticResource
     * @param partialPath
     * @return
     */
    private Resource resolveResourceFromFilePath(PluginStaticResource pluginStaticResource, String partialPath){
        Set<String> filePaths = pluginStaticResource.getFilePaths();
        if(filePaths == null || filePaths.isEmpty()){
            return null;
        }

        for(String filePath : filePaths){
            Path fullPath = Paths.get(filePath, partialPath);
            if(!Files.exists(fullPath)){
                return null;
            }
            try {
                FileUrlResource fileUrlResource = new FileUrlResource(fullPath.toString());
                if(fileUrlResource.exists()){
                    return fileUrlResource;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                if(log.isDebugEnabled()) {
                    log.debug("获取静态资源 [{}] 失败", fullPath, e);
                }
            }
        }
        return null;
    }

    /**
     * 通过staticResourceConfig解析静态资源文件
     *
     * @param pluginRegistryInfo
     * @param staticResourceConfig
     */
    public static void parse(PluginRegistryInfo pluginRegistryInfo, SpringBootStaticResourceConfig staticResourceConfig) {
        if (pluginRegistryInfo == null || staticResourceConfig == null) {
            return;
        }

        Set<String> locations = staticResourceConfig.locations();
        if (locations == null || locations.isEmpty()) {
            return;
        }

        String pluginId = pluginRegistryInfo.getPluginWrapper().getPluginId();

        Set<String> fileLocations = Sets.newHashSet();
        Set<String> classpathLocations = Sets.newHashSet();

        for (String location : locations) {
            int separatorIndex = location.indexOf(":");
            if (separatorIndex == -1) {
                continue;
            }

            String locationType = location.substring(0, separatorIndex);
            String locationPath = location.substring(separatorIndex + 1);

            if ("classpath".equalsIgnoreCase(locationType)) {
                if (locationPath.startsWith("/")) {
                    locationPath = locationPath.substring(1);
                }
                if (!locationPath.endsWith("/")) {
                    locationPath = locationPath + "/";
                }
                classpathLocations.add(locationPath);
            } else if ("file".equalsIgnoreCase(locationType)) {
                if (!locationPath.endsWith(File.separator)) {
                    locationPath = locationPath + File.separator;
                }
                fileLocations.add(locationPath);
            } else {
                log.warn("插件" + pluginId + "存在未知的静态资源配置路径类型 ：" + location);
            }
        }

        PluginStaticResource pluginStaticResource = new PluginStaticResource();
        pluginStaticResource.setClassPaths(classpathLocations);
        pluginStaticResource.setFilePaths(fileLocations);
        pluginStaticResource.setPluginRegistryInfo(pluginRegistryInfo);

        PLUGIN_STATIC_RESOURCE_MAP.put(pluginId, pluginStaticResource);
    }

    /**
     * 移除插件扩展的静态资源信息
     *
     * @param pluginId
     */
    public static void remove(String pluginId) {
        PLUGIN_STATIC_RESOURCE_MAP.remove(pluginId);
    }

    /**
     * 插件静态资源类
     */
    static class PluginStaticResource {
        /**
         * 基础插件信息
         */
        private PluginRegistryInfo pluginRegistryInfo;

        /**
         * 定义的classpath集合
         */
        private Set<String> classPaths;

        /**
         * 定义的文件路径集合
         */
        private Set<String> filePaths;

        /**
         * 缓存的资源。key 为资源的可以。值为资源
         */
        private Map<String, Resource> cacheResourceMaps = new ConcurrentHashMap<>();

        public PluginRegistryInfo getPluginRegistryInfo() {
            return pluginRegistryInfo;
        }

        public void setPluginRegistryInfo(PluginRegistryInfo pluginRegistryInfo) {
            this.pluginRegistryInfo = pluginRegistryInfo;
        }

        public Set<String> getClassPaths() {
            return classPaths;
        }

        public void setClassPaths(Set<String> classPaths) {
            this.classPaths = classPaths;
        }

        public Set<String> getFilePaths() {
            return filePaths;
        }

        public void setFilePaths(Set<String> filePaths) {
            this.filePaths = filePaths;
        }

        public boolean hasCacheResource(String pluginId) {
            return cacheResourceMaps.containsKey(pluginId);
        }

        public Resource getCacheResource(String pluginId) {
            return cacheResourceMaps.get(pluginId);
        }

        public void addCacheResourceIfAbsent(String pluginId, Resource resource) {
            if (!cacheResourceMaps.containsKey(pluginId)) {
                log.warn("插件 [{}] 的静态资源 [{}] 已存在",pluginId,resource.toString());
                cacheResourceMaps.put(pluginId, resource);
            }

        }
    }
}
