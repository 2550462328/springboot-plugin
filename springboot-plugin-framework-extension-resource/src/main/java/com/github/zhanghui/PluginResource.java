package com.github.zhanghui;

import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.springframework.core.NestedIOException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Description:
 * 从插件的环境中获取到的资源信息
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/8 13:58
 **/
@Slf4j
public class PluginResource implements Resource {

    private ClassLoader classLoader;

    private PluginWrapper pluginWrapper;

    /**
     * 最近修改时间
     */
    private final long lastModified;

    /**
     * 资源加载路径
     */
    private final String path;

    public PluginResource(String path, PluginRegistryInfo pluginRegistryInfo) {
        String pathToUse = StringUtils.cleanPath(path);
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        this.path = pathToUse;

        this.classLoader = pluginRegistryInfo.getDefaultPluginClassLoader();
        this.pluginWrapper = pluginRegistryInfo.getPluginWrapper();
        this.lastModified = pluginRegistryInfo.getBasePlugin().getBasePluginExtension().getStartTimestamp();
    }

    @Override
    public boolean exists() {
        try {
            URL url = getURL();
            if (url == null) {
                return false;
            }
            if (ResourceUtils.isFileURL(url)) {
                return getFile().exists();
            }

            if (contentLength() > 0) {
                return true;
            }
            // 没有抛出IOException说明Resource是存在的
            InputStream inputStream = getInputStream();
            inputStream.close();
            return true;
        } catch (IOException e) {
            log.debug("Resource [{}] 不存在", getDescription());
            return false;
        }
    }

    @Override
    public URL getURL() throws IOException {
        return classLoader.getResource(path);
    }

    @Override
    public URI getURI() throws IOException {
        URL url = getURL();

        try {
            return ResourceUtils.toURI(url);
        } catch (URISyntaxException e) {
            throw new NestedIOException("无效的URL [" + url + "]", e);
        }
    }

    @Override
    public File getFile() throws IOException {
        URL url = getURL();
        if (ResourceUtils.isJarURL(url)) {
            URL archiveURL = ResourceUtils.extractArchiveURL(url);
            return ResourceUtils.getFile(archiveURL, "Jar File");
        }
        return ResourceUtils.getFile(url, getDescription());
    }

    @Override
    public long contentLength() throws IOException {
        URL url = getURL();
        if (ResourceUtils.isFileURL(url)) {
            return getFile().length();
        } else if (ResourceUtils.isJarURL(url)) {
            URLConnection urlConnection = getURL().openConnection();
            return urlConnection.getContentLength();
        }
        return 0;
    }

    @Override
    public long lastModified() throws IOException {
        return lastModified;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new RuntimeException("当前Resource不支持createRelative操作");
    }

    @Override
    public String getFilename() {
        return StringUtils.getFilename(path);
    }

    @Override
    public String getDescription() {
        return pluginWrapper.getDescriptor().getPluginDescription();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return classLoader.getResourceAsStream(path);
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
