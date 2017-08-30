package com.ycd.util.file;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by yangchd on 2017/7/24.
 * 文件接口
 */

public interface IFileService {
    boolean uploadFile(MultipartFile file, HttpServletRequest request) throws Exception;
}
