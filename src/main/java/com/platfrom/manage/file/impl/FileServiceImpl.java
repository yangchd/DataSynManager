package com.platfrom.manage.file.impl;

import com.platfrom.manage.file.IFileService;
import com.platfrom.manage.datasyn.DataSynTools;
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
            String savePath = new DataSynTools().getWebPath(request)+"upload";
//          转存文件
            file.transferTo(new File(savePath + "/" + filename));
            flag = true;
        }
        return flag;
    }
}
