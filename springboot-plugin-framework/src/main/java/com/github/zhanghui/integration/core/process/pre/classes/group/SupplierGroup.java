package com.github.zhanghui.integration.core.process.pre.classes.group;

import com.github.zhanghui.annotation.Supplier;
import com.github.zhanghui.integration.core.process.pre.classes.PluginClassGroup;
import com.github.zhanghui.realize.BasePlugin;
import com.github.zhanghui.utils.AnnotationUtils;

/**
 * 分组存在注解: @Supplier
 *
 * @author starBlues
 * @version 2.1.0
 */
public class SupplierGroup implements PluginClassGroup {

    /**
     * 自定义 @Supplier
     */
    public static final String GROUP_ID = "supplier";


    @Override
    public String groupId() {
        return GROUP_ID;
    }

    @Override
    public void initialize(BasePlugin basePlugin) {

    }

    @Override
    public boolean filter(Class<?> aClass) {
        return AnnotationUtils.hasAnnotation(aClass, false, Supplier.class);
    }

}
