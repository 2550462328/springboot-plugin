package com.github.zhanghui.thymeleaf;

import lombok.Getter;
import lombok.Setter;
import org.thymeleaf.templatemode.TemplateMode;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/8 15:02
 **/
@Getter
@Setter
public class ThymeleafConfig {

    private static final String DEFAULT_PREFIX = "templates/";

    private static final String DEFAULT_SUFFIX = ".html";

    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    /**
     * 存放模板引擎的前缀
     */
    private String prefix = DEFAULT_PREFIX;

    /**
     * 模板引擎文件的后缀
     */
    private String suffix = DEFAULT_SUFFIX;

    /**
     * 模型引入的模型
     * @see TemplateMode
     */
    private TemplateMode mode = TemplateMode.HTML;

    /**
     * 模板引擎的编码
     */
    private Charset encoding = DEFAULT_ENCODING;

    /**
     * 是否启用模板引擎的缓存
     */
    private boolean cache = true;

    /**
     * 模板解析器的执行顺序, 数字越小越先执行
     */
    private Integer templateResolverOrder;


}
