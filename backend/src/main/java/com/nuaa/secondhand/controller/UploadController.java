package com.nuaa.secondhand.controller;

import com.nuaa.secondhand.dto.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/** 图片上传（存本地 uploads/ 目录） */
@RestController
@RequestMapping("/upload")
public class UploadController {

    @PostMapping
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("文件为空");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        // 用绝对路径，避免相对路径被 Tomcat 解析到 work 临时目录
        File dir = new File(System.getProperty("user.dir"), "uploads");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(dir, filename).getAbsoluteFile());
        } catch (IOException e) {
            return Result.fail("上传失败：" + e.getMessage());
        }
        return Result.ok("/uploads/" + filename);
    }
}
