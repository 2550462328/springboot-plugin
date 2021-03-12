package com.mybatisplus.main;

import com.github.zhanghui.integration.core.process.post.extension.SpringDocControllerProcessorExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 主启动程序
 *
 * @author starBlues
 * @version 1.0
 */
@SpringBootApplication()
@Import(SpringDocControllerProcessorExtension.class)
public class TkMybatisMain {

    public static void main(String[] args) {
        SpringApplication.run(TkMybatisMain.class, args);
    }

}
