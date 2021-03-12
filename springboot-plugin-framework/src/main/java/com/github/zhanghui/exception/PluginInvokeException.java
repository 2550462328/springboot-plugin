package com.github.zhanghui.exception;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 10:57
 **/
public class PluginInvokeException extends RuntimeException {

    public PluginInvokeException(String errorMsg){
        super(errorMsg);
    }

    public PluginInvokeException(String errorMsg, Throwable throwable){
        super(errorMsg,throwable);
    }

}
