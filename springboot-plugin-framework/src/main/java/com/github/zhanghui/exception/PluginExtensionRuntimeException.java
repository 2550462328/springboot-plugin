package com.github.zhanghui.exception;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 10:57
 **/
public class PluginExtensionRuntimeException extends RuntimeException {

    public PluginExtensionRuntimeException(String errorMsg){
        super(errorMsg);
    }

    public PluginExtensionRuntimeException(String errorMsg, Throwable throwable){
        super(errorMsg,throwable);
    }

}
