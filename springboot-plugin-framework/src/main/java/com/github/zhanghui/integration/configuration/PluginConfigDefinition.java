package com.github.zhanghui.integration.configuration;

import java.util.Objects;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 16:44
 **/
public class PluginConfigDefinition {

    private final String fileName;

    private final Class configClass;

    public PluginConfigDefinition(String fileName, Class configClass) {
        Objects.requireNonNull(fileName,"配置文件名不能为空");
        Objects.requireNonNull(configClass,"配置文件类不能为空");
        this.fileName = fileName;
        this.configClass = configClass;
    }

    public String getFileName() {
        return fileName;
    }

    public Class getConfigClass() {
        return configClass;
    }
}
