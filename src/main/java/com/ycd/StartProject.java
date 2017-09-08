package com.ycd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.web.WebApplicationInitializer;

/**
 * Created by yangchd on 2017/7/21.
 * spring boot启动类
 */

//@ComponentScan(value = {"com.ycd"})
//@EnableAutoConfiguration
@SpringBootApplication
public class StartProject {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(StartProject.class, args);
    }
}
//public class StartProject extends SpringBootServletInitializer implements WebApplicationInitializer {
//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//        return application.sources(StartProject.class);
//    }
//
//    public static void main(String[] args) throws Exception {
//        SpringApplication.run(StartProject.class, args);
//    }
//}
