package com.neko.reggie.controller;

import com.neko.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@ResponseBody
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String bassPath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // file是一个临时文件，需要转存到其它位置，否则本次请求完成后临时文件会删除
        log.info("file = {}", file);

        // 原始文件
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        // 创建一个目录对象
        File dir = new File(bassPath);

        // 判断当前目录是否存在
        if (!dir.exists()) {
            // 若当前目录不存在，则创建目录
            if (dir.mkdir()) {
                log.info("创建目录成功...");
            }
        }

        try {
            // 把临时文件转存到指定位置
            file.transferTo(new File(bassPath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        // 输入流，通过输入流读取文件内容
        FileInputStream fileInputStream = null;

        // 输出流，通过输出流将文件写回给游览器，游览器回显图片
        ServletOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(bassPath + name));

            fileOutputStream = response.getOutputStream();
            // 设置服务器响应给游览器文件类型为图片
            response.setContentType("image/jpeg");

            int len;
            byte[] bytes = new byte[1024 * 20];

            while((len = fileInputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, len);
                fileOutputStream.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
