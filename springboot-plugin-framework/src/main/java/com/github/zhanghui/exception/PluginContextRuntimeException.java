package com.github.zhanghui.exception;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 10:57
 **/
public class PluginContextRuntimeException extends RuntimeException {

    public PluginContextRuntimeException(String errorMsg){
        super(errorMsg);
    }

    public PluginContextRuntimeException(String errorMsg, Throwable throwable){
        super(errorMsg,throwable);
    }

}
