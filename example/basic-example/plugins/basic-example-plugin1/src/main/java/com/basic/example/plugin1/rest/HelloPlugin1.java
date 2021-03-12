package com.basic.example.plugin1.rest;

import com.basic.example.plugin1.config.ConfigBean;
import com.basic.example.plugin1.config.PluginConfig1;
import com.basic.example.plugin1.service.HelloService;
import com.github.zhanghui.realize.PluginUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.pf4j.PluginDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 插件接口
 *
 * @author starBlues
 * @version 1.0
 */
@RestController
@RequestMapping(path = "plugin1")
@Api(value = "插件接口", description = "插件hello案例")
public class HelloPlugin1 {

    @Autowired
    private HelloService helloService;

    @Autowired
    private PluginConfig1 pluginConfig1;

    @Autowired
    private PluginUtils pluginUtils;

    @Autowired
    private ConfigBean.ConfigBeanTest configBeanTest;

    @GetMapping("hello")
    @ApiOperation(value = "hello", notes = "hello")
    public String sya(){
        return "hello plugin1 example";
    }

    @GetMapping("config")
    @ApiOperation(value = "getConfig", notes = "得到配置文件")
    public String getConfig(){
        return pluginConfig1.toString();
    }


    @GetMapping("serviceConfig")
    public String getServiceConfig(){
        return helloService.getPluginConfig1().toString();
    }

    @GetMapping("service")
    public String getService(){
        return helloService.sayService2();
    }

    @GetMapping("pluginInfo")
    public PluginDescriptor getPluginInfo(){
        return pluginUtils.getPluginDescriptor();
    }

    @GetMapping("configBeanTest")
    public ConfigBean.ConfigBeanTest getConfigBeanTest(){
        return configBeanTest;
    }

}
