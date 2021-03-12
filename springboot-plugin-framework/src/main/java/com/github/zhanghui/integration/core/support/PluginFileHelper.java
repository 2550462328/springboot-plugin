package com.github.zhanghui.integration.core.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Description:
 * 对配置插件目录下文件的相关操作
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/4 16:54
 **/
public class PluginFileHelper {

    /**
     * 清除path路径下的空文件
     * @param path
     */
    public static void cleanEmptyFile(Path path){
        Objects.requireNonNull(path,"待处理的文件路径不能为空");

        if(path == null || !Files.exists(path)){
            return;
        }

        try {
            Files.list(path).forEach(subPath ->{
                File subFile = subPath.toFile();
                if(!subFile.isFile()){
                    return;
                }
                long fileLength = subFile.length();
                if(fileLength == 0){
                    try {
                        Files.deleteIfExists(subPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
