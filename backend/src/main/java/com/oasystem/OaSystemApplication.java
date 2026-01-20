package com.oasystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * OA办公系统启动类
 */
@SpringBootApplication
@MapperScan("com.oasystem.modules.*.mapper")
@EnableScheduling
public class OaSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaSystemApplication.class, args);
        System.out.println("===========================================");
        System.out.println("    OA办公系统启动成功！");
        System.out.println("    API文档: http://localhost:9090/api/doc.html");
        System.out.println("===========================================");
    }

}
