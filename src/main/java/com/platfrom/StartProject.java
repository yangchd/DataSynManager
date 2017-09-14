package com.platfrom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by yangchd on 2017/7/21.
 * spring boot启动类
 */

//@EnableAutoConfiguration
//@ComponentScan(value = {"com.platfrom.manage"})
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
