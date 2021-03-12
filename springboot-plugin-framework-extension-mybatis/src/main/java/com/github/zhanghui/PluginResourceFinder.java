package com.github.zhanghui;

import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Description:
 * 针对mybatis的配置 查找配置中声明的资源信息并托管
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/2 20:08
 **/
public class PluginResourceFinder {

    private static final String RESOURCE_TYPE_PACKAGE = "package";

    private final ClassLoader classLoader;
    private final ResourcePatternResolver resourcePatternResolver;
    private final MetadataReaderFactory metadataReaderFactory;

    public PluginResourceFinder(PluginRegistryInfo pluginRegistryInfo) {
        this.classLoader = pluginRegistryInfo.getDefaultPluginClassLoader();
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver(classLoader);
        this.metadataReaderFactory = new SimpleMetadataReaderFactory();
    }

    /**
     * 获取xmlLocationsMatchSet中的Resource（xml资源）
     *
     * @param xmlLocationsMatchSet 待匹配的xml资源路径集合
     */
    public Resource[] getXmlResources(Set<String> xmlLocationsMatchSet) throws IOException {

        if (xmlLocationsMatchSet == null || xmlLocationsMatchSet.size() == 0) {
            return null;
        }
        List<Resource> xmlLoadResources = Lists.newArrayList();
        for (String xmlLocation : xmlLocationsMatchSet) {

            if (StringUtils.isNotBlank(xmlLocation)) {
                xmlLoadResources.addAll(getXmlResources(xmlLocation));
            }
        }

        if (xmlLoadResources.isEmpty()) {
            return null;
        }
        return xmlLoadResources.toArray(new Resource[0]);
    }


    /**
     * 获取xmlLocationMatch下的Resource（xml资源）
     *
     * @param xmlLocationMatch 待匹配的xml资源路径
     */
    private List<Resource> getXmlResources(String xmlLocationMatch) throws IOException {

        String[] xmlLocationArray = xmlLocationMatch.split(":");
        String resourceType = xmlLocationArray[0];
        String xmlLocationPath = xmlLocationArray[1];

        if (StringUtils.equals(resourceType, RESOURCE_TYPE_PACKAGE)) {
            xmlLocationPath = xmlLocationPath.replaceAll("\\.", "/");
        }

        Resource[] loadResources = resourcePatternResolver.getResources(xmlLocationPath);
        if (loadResources.length > 0) {
            return Arrays.asList(loadResources);
        }
        return null;
    }

    /**
     * 获取插件的实体类及其别名
     *
     * @param packagePatterns 实体类包名
     */
    public Class<?>[] getAliasesClasses(Set<String> packagePatterns) throws IOException {

        if(packagePatterns == null || packagePatterns.isEmpty()){
            return null;
        }

        Set<Class<?>> packageEntities = Sets.newHashSet();

        for(String packagePattern : packagePatterns){
            String resourcePath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(packagePattern) + "/**/*.class";

            Resource[] packageLoadResources = resourcePatternResolver.getResources(resourcePath);
            for(Resource resource : packageLoadResources){
                try {
                    // 读取Resource信息
                    ClassMetadata classMetadata = metadataReaderFactory.getMetadataReader(resource).getClassMetadata();
                    Class<?> clazz = classLoader.loadClass(classMetadata.getClassName());
                    packageEntities.add(clazz);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        if(packageEntities.isEmpty()){
            return null;
        }

        return packageEntities.toArray(new Class[0]);

    }
}
