package com.cxn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * @program: elasticsearch-demo
 * @description: ${description}
 * @author: cxn
 * @create: 2018-06-20 14:45
 * @Version v1.0
 */
public class ReadUUIDToMemery {

    public static void main(String[] args) throws Exception {
        File uuidFile = new File("/Users/caoxunan/learn-git/elasticsearch-demo/jsonfile/uuidFile");

        FileReader fileReader = new FileReader(uuidFile);

        BufferedReader bufferedReader = new BufferedReader(fileReader);

        long start = System.currentTimeMillis();
        List<String> uuidList = new ArrayList<>(30000000);
        long end = System.currentTimeMillis();
        System.out.println("集合初始化："  +  (end - start) + "ms");

        long readFileToMemeryStart = System.currentTimeMillis();

        String temp = "";
        int i = 0;
        while ((temp = bufferedReader.readLine()) != null) {
            // System.out.println("temp" + temp);
            uuidList.add(temp);
            i++;
            if (i % 1000000 == 0){
                System.out.println(i);
                if (i == 10000000){
                    break;
                }
            }
        }

        System.out.println("uuidList size():" + uuidList.size());
        long readFileToMemeryEnd = System.currentTimeMillis();

        System.out.println("read book to memery cost : " + (readFileToMemeryEnd - readFileToMemeryStart) / 1000 + "s");
        // 创建随机id模拟图片搜索引擎返回具体数据
        Set<String> set = new HashSet<>(32);
        int num = 30;
        while (set.size() < num) {
            Random random = new Random();
            int rand = random.nextInt(10000000);
            set.add(uuidList.get(rand));
        }

        String[] uuidArr = set.toArray(new String[0]);
        String result = Arrays.toString(uuidArr);
        System.out.println("rand uuid :" + result);
    }

}
