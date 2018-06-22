package com.cxn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;

/**
 * @program: elasticsearch-demo
 * @description: ${description}
 * @author: cxn
 * @create: 2018-06-19 13:54
 * @Version v1.0
 */
public class GenerateJsonFile {

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        File file = new File("/Users/caoxunan/learn-git/elasticsearch-demo/jsonfile/import_json_uuid_1.json");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        File uuidFile = new File("/Users/caoxunan/learn-git/elasticsearch-demo/jsonfile/uuidFile");
        // 构建字节输出流
        FileOutputStream ops = new FileOutputStream(file);

        Writer uuidWriter = new OutputStreamWriter(new FileOutputStream(uuidFile));

        // 构建字符输出流
        OutputStreamWriter writer = new OutputStreamWriter(ops, "UTF-8");

        // 防止文件过大，切割文件
        int j = 1;
        for (int i = 1; i <= 50000000; i++) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            // 写入文件
            writer.append("{\"index\":{\"_index\":\"uuidsearch\",\"_type\":\"test\"}}");
            //writer.append("{\"index\":{\"_index\":\"imagesearch\",\"_type\":\"test\",\"_id\":\""+ i +"\"}}");

            // 换行,注意：window中换行为\r\n Linux中为\n
            // writer.append("\r\n");
            writer.append("\n");
            writer.append("{\"id\":\"" + uuid + "\",\"url\":\"http://image.zhihuiya.com/file/" + i + "\"}");
            //writer.append("{\"id\":\""+ i + "\",\"url\":\"http://image.zhihuiya.com/file/" + i + "\"}");
            // 换行
            // writer.append("\r\n");
            writer.append("\n");

            uuidWriter.write(uuid);
            uuidWriter.write("\n");

            // 每百条数据刷新一次缓存
            if (i % 100 == 0) {
                writer.flush();
                uuidWriter.flush();
            }
            // 每25万条数据写入一个文件
            if (i % 250000 == 0) {
                j++;
                // 关闭写入流，同时会把缓冲区的内容写入文件
                writer.close();
                ops.close();

                if (i != 50000000) {
                    file = new File("/Users/caoxunan/learn-git/elasticsearch-demo/jsonfile/import_json_uuid_" + j + ".json");
                    ops = new FileOutputStream(file);
                    writer = new OutputStreamWriter(ops);
                }
            }
            if (i % 1000000 == 0) {
                System.out.println("已完成" + i + "条数据的生成");
            }

        }// for loop end

        writer.close();
        ops.close();
        uuidWriter.close();
        long end = System.currentTimeMillis();

        System.out.println("File generate success～, cost time " + (end - start) / 1000 + "s");
    }

}

