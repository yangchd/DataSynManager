package com.ycd.springboot.service.file.impl;

import com.ycd.springboot.service.file.IFileService;
import com.ycd.springboot.util.Tools;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

/**
 * Created by yangchd on 2017/7/24.
 * 文件接口实现类
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    @Override
    public boolean uploadFile(MultipartFile file,HttpServletRequest request)
            throws IOException {
        boolean flag = false;
        if (!file.isEmpty()) {
//          文件保存路径
            String filename = file.getOriginalFilename();
            String savePath = new Tools().getWebPath(request)+"upload";
//          转存文件
            file.transferTo(new File(savePath + "/" + filename));
            flag = true;
        }
        return flag;
    }
}
