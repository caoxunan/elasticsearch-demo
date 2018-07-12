package com.cxn.elasticsearch.controller;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @program: elasticsearch-demo
 * @description: ${description}
 * @author: cxn
 * @create: 2018-06-21 10:03
 * @Version v1.0
 */
@Controller
public class UploadController {

    // 下载文件
    @RequestMapping("/download")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public void download1(HttpServletResponse response) throws Exception {
        String filePath = "file/SpringMVC.xmind";
        File file = new File(filePath);
        OutputStream out = null;
        try {
            response.reset();
            response.setContentType("application/octet-stream; charset=utf-8");
            //设置响应头和客户端保存文件名
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            out = response.getOutputStream();
            out.write(FileUtils.readFileToByteArray(file));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("下载完成！");
    }

}
