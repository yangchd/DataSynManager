package com.platfrom.manage.file.controller;

import com.platfrom.manage.file.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangchd on 2017/7/21.
 * 文件上传
 */

@Controller
@RequestMapping(value="/file")
public class FileController {

    @Autowired
    public IFileService iFileService;

    @RequestMapping(value="/upload")
    @ResponseBody
    public Map<String,Object> upLoadFile(@RequestParam("file") MultipartFile file,
                                         HttpServletRequest request, HttpServletResponse response){
        Map<String,Object> rMap = new HashMap<>();
        Map<String,Object> rtMap = new HashMap<>();
        try {
            iFileService.uploadFile(file,request);
            rtMap.put("retflag","0");
            rtMap.put("msg","文件"+file.getOriginalFilename()+"上传成功");
        } catch (Exception e) {
            rtMap.put("retflag","1");
            rtMap.put("msg","文件"+file.getOriginalFilename()+"上传失败"+e.getMessage());
        }
        rMap.put("result", rtMap);
        return rMap;
    }
}
