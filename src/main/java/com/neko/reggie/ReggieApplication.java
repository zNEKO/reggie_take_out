package com.neko.reggie;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@Slf4j
@SpringBootApplication
// Mapper
@MapperScan("com.neko.reggie.mapper")
// Filter
@ServletComponentScan
public class ReggieApplication{
    public static void main(String[] args) {
//        new SpringApplicationBuilder()
//                .sources(ReggieApplication.class)
//                .run(args);
        SpringApplication.run(ReggieApplication.class);
        log.info("项目启动成功......");
    }

}
