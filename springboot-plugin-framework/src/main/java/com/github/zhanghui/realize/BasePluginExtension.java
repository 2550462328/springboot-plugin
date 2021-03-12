package com.github.zhanghui.realize;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/22 17:36
 **/
public class BasePluginExtension {

    /**
     * 记录插件的一些事件时间点
     */
    private Long startTimestamp;

    private Long stopTimestamp;

    void startEvent(){
        startTimestamp = System.currentTimeMillis();
    }

    void deleteEvent(){
        stopTimestamp = System.currentTimeMillis();
    }

    void stopEvent(){
        stopTimestamp = System.currentTimeMillis();
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }
}
