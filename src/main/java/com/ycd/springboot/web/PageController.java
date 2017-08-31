package com.ycd.springboot.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by yangchd on 2017/7/21.
 * spring boot 在这里配置所有的页面请求路径
 */

@Controller
public class PageController {
    @RequestMapping("uploadfile")
    public String uploadFile(){
        return "/file/uploadfile";
    }

    @RequestMapping("datasyn")
    public String dataSyn(){
        return "/data/datasyn";
    }
}