package com.mybatis.plugin2.config;


import com.github.zhanghui.annotation.ConfigDefinition;

/**
 * description
 *
 * @author starBlues
 * @version 1.0
 */
@ConfigDefinition("plugin2.yml")
public class Plugin2Config {

    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Plugin2Config{" +
                "name='" + name + '\'' +
                '}';
    }
}
