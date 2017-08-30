package com.ycd.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by yangchd on 2017/7/21.
 * spring boot启动类
 */

@ComponentScan(value = {"com.ycd"})
@EnableAutoConfiguration
public class StartProject {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(StartProject.class, args);
    }
}
