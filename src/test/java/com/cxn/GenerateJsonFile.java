package com.cxn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

/**
 * @program: elasticsearch-demo
 * @description: ${description}
 * @author: cxn
 * @create: 2018-06-19 13:54
 * @Version v1.0
 */
public class GenerateJsonFile {

    public static void main(String[] args) throws Exception{

        long start = System.currentTimeMillis();

        File file = new File("/Users/caoxunan/learn-git/elasticsearch-demo/jsonfile/import_json1.json");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        // 构建字节输出流
        FileOutputStream ops = new FileOutputStream(file);
        // 构建字符输出流
        OutputStreamWriter writer = new OutputStreamWriter(ops, "UTF-8");

        // 防止文件过大，切割文件
        int j = 1;
        for (int i = 1; i <= 50000000; i++) {
            // String uuid = UUID.randomUUID().toString().replace("-", "");
            // 写入文件
            // writer.append("{\"index\":{\"_index\":\"imagesearch\",\"_type\":\"test\",\"_id\":\""+ uuid +"\"}}");
            writer.append("{\"index\":{\"_index\":\"imagesearch\",\"_type\":\"test\",\"_id\":\""+ i +"\"}}");
            // 换行
            writer.append("\r\n");
            // writer.append("{\"id\":\""+ uuid + "\",\"url\":\"http://image.zhihuiya.com/file/i}\"");
            writer.append("{\"id\":\""+ i + "\",\"url\":\"http://image.zhihuiya.com/file/i}\"");
            // 换行
            writer.append("\r\n");
            // 每百条数据刷新一次缓存
            if (i % 100 == 0) {
                writer.flush();
            }
            // 每25万条数据写入一个文件
            if (i % 250000 == 0) {
                j++;
                // 关闭写入流，同时会把缓冲区的内容写入文件
                writer.close();
                ops.close();

                if (i != 50000000){
                    file = new File("/Users/caoxunan/learn-git/elasticsearch-demo/jsonfile/import_json"+ j +".json");
                    ops = new FileOutputStream(file);
                    writer = new OutputStreamWriter(ops);
                }
            }
            if (i % 1000000 == 0) {
                System.out.println("已完成" + i + "条数据的生成");
            }

        }// for loop end

        long end = System.currentTimeMillis();

        System.out.println("File generate success～, cost time " + (start - end)/1000 + "s");
    }

}

