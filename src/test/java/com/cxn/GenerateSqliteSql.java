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
 * @create: 2018-06-22 13:40
 * @Version v1.0
 */
public class GenerateSqliteSql {

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        File file = new File("/Users/caoxunan/learn-git/elasticsearch-demo/sqlitefile/import_sql_1.sql");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        File uuidFile = new File("/Users/caoxunan/learn-git/elasticsearch-demo/sqlitefile/uuidFile");
        // 构建字节输出流
        FileOutputStream ops = new FileOutputStream(file);

        Writer uuidWriter = new OutputStreamWriter(new FileOutputStream(uuidFile));

        // 构建字符输出流
        OutputStreamWriter writer = new OutputStreamWriter(ops, "UTF-8");

            writer.append("PRAGMA foreign_keys=OFF;\n" +
                    "BEGIN TRANSACTION;\n" +
                    "CREATE TABLE imageurl(\n" +
                    "id text,\n" +
                    "url text);\n");
        // 防止文件过大，切割文件
        int j = 1;
        for (int i = 1; i <= 50000000; i++) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            // 写入文件
            // 换行,注意：window中换行为\r\n Linux中为\n
            writer.append("INSERT INTO imageurl VALUES('"+ uuid +"','http://image.zhihuiya.com/search/"+ i +"');\n");
            // 换行
            uuidWriter.write(uuid);
            uuidWriter.write("\n");

            // 每百条数据刷新一次缓存
            if (i % 100 == 0) {
                writer.flush();
                uuidWriter.flush();
            }
            // 每25万条数据写入一个文件
            if (i % 500000 == 0) {
                writer.append("COMMIT;");
                j++;
                // 关闭写入流，同时会把缓冲区的内容写入文件
                writer.close();
                ops.close();

                if (i != 50000000) {
                    file = new File("/Users/caoxunan/learn-git/elasticsearch-demo/sqlitefile/import_sql_" + j + ".sql");
                    ops = new FileOutputStream(file);
                    writer = new OutputStreamWriter(ops);
                    writer.append("BEGIN TRANSACTION;\n");
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
