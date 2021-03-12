package com.github.zhanghui;

/**
 * Description:
 * 生命周期接口
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/4 15:56
 **/
public interface Lifecycle {

    /**
     * 初始化之前
     */
    void before();

    /**
     * 初始化完成
     */
    void complete();

    /**
     * 初始化失败
     * @param throwable 异常
     */
    void failure(Throwable throwable);
}
