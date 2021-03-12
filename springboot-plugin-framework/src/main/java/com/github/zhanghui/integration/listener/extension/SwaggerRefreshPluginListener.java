package com.github.zhanghui.integration.listener.extension;

import com.github.zhanghui.integration.listener.PluginListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;

/**
 * Description:
 * 对新启用和停止的插件 的swagger Api即时进行更新
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/5 9:39
 **/
@Slf4j
public class SwaggerRefreshPluginListener implements PluginListener {

    private final ApplicationContext mainApplicationContext;

    public SwaggerRefreshPluginListener(ApplicationContext mainApplicationContext) {
        this.mainApplicationContext = mainApplicationContext;
    }

    /**
     * 监听插件的注册事件
     *
     * @param pluginId 插件Id
     * @param isStartInitial 是否是插件初次初始化时的注册事件
     */
    @Override
    public void registry(String pluginId, boolean isStartInitial) {
        if(isStartInitial){
            return;
        }
        refresh();
    }

    /**
     * 监听插件的注销事件
     *
     * @param pluginId
     */
    @Override
    public void unRegistry(String pluginId) {
        refresh();
    }

    /**
     * 监听插件的异常事件
     *
     * @param pluginId
     * @param throwable
     */
    @Override
    public void failure(String pluginId, Throwable throwable) {

    }

    /**
     * 刷新Swagger的上下文
     */
    private void refresh(){
        DocumentationPluginsBootstrapper documentationPluginsBootstrapper = mainApplicationContext.getBean(DocumentationPluginsBootstrapper.class);
        try {
            if(documentationPluginsBootstrapper != null){
                documentationPluginsBootstrapper.stop();
                documentationPluginsBootstrapper.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("swagget api实时更新失败：[{}]",e.getMessage(),e);
        }
    }

}
