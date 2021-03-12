package com.mybatis.plugin1.validator;

import com.github.zhanghui.realize.PluginUtils;
import com.github.zhanghui.realize.UnRegistryValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/1/28 16:16
 **/
@Component
public class MyUnRegistryValidator implements UnRegistryValidator {

    @Autowired
    private PluginUtils pluginUtils;

    @Override
    public Result verify() throws Exception {
        return new Result(true);
//        PluginUser pluginUser = pluginUtils.getMainBean(PluginUser.class);
//
//        MyUnRegistryValidator myUnRegistryValidator = pluginUser.getBean("myUnRegistryValidator");
//
//        return new Result(myUnRegistryValidator != null, "啊，这种无限循环找不到");
    }
}
