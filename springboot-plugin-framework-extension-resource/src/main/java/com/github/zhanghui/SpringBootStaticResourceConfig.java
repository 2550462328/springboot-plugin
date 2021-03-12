package com.github.zhanghui;

import java.util.Set;

/**
 * Description:
 * 插件静态资源的相关配置
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/8 10:19
 **/
public interface SpringBootStaticResourceConfig {

    /**
     * 资源访问路径配置，如：
     * file:// ** 或 classpath: /**
     */
    Set<String> locations();
}
