package com.github.zhanghui.realize;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * Description:
 * Plugin is the base class for all plugins types. Each plugin is loaded into a separate class loader to avoid conflicts
 * PLUGIN = a container for EXTENSION POINTS and EXTENSIONS + lifecycle methods (start, stop, delete)
 * BasePlugin = Plugin + Other settings
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/6 14:34
 **/
public class BasePlugin extends Plugin {

    private final BasePluginExtension basePluginExtension;

    public BasePlugin(PluginWrapper wrapper) {
        super(wrapper);
        this.basePluginExtension = new BasePluginExtension();
    }

    @Override
    public void start() {
        startEvent();
        basePluginExtension.startEvent();
    }

    @Override
    public void stop() {
        stopEvent();
        basePluginExtension.stopEvent();
    }

    @Override
    public void delete() {
        deleteEvent();
        basePluginExtension.deleteEvent();
    }

    public String getScanPackage(){
        return this.getClass().getPackage().getName();
    }


    /**
     * 启动事件. Spring 容器都没有准备。无法使用注入。
     */
    protected void startEvent(){

    }

    /**
     * 删除事件. 在插件删除时触发。
     */
    protected void deleteEvent(){

    }

    /**
     * 停止事件. 在插件停止时触发。
     */
    protected void stopEvent(){

    }

    public BasePluginExtension getBasePluginExtension() {
        return basePluginExtension;
    }
}
