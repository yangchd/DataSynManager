package com.yangchd.data.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yangchd
 * @date 2017/7/21
 * spring boot 在这里配置所有的页面请求路径
 */

@Controller
public class PageController {

    @RequestMapping("dataSyn")
    public String dataSyn(){
        return "/data/dataSyn";
    }

}