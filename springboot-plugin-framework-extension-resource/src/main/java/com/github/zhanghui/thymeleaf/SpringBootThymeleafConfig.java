package com.github.zhanghui.thymeleaf;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/8 15:02
 **/
public interface SpringBootThymeleafConfig {

    /**
     * 自定义配置Thymeleaf
     * @param thymeleafConfig
     */
    void config(ThymeleafConfig thymeleafConfig);
}
