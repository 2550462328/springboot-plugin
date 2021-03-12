package com.mybatis.main;

import com.github.zhanghui.integration.core.process.post.extension.SpringDocControllerProcessorExtension;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * description
 *
 * @author starBlues
 * @version 1.0
 */
@SpringBootApplication(
        scanBasePackages = { "com.mybatis.main" })
@MapperScan("com.mybatis.main.mapper")
@Import(SpringDocControllerProcessorExtension.class)
public class IntegrationMybatisMain {

    public static void main(String[] args) {

        SpringApplication.run(IntegrationMybatisMain.class, args);
    }

}
