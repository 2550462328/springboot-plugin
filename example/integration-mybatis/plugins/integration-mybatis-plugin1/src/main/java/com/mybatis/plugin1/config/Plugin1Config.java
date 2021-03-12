package com.mybatis.plugin1.config;

import com.github.zhanghui.annotation.ConfigDefinition;
import org.springframework.beans.factory.annotation.Value;

/**
 * description
 *
 * @author starBlues
 * @version 1.0
 */
public class Plugin1Config {

    @Value("name")
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
