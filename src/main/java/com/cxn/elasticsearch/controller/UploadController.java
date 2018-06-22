package com.cxn.elasticsearch.controller;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @program: elasticsearch-demo
 * @description: ${description}
 * @author: cxn
 * @create: 2018-06-21 10:03
 * @Version v1.0
 */
@Controller
public class UploadController {

    static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

    /**
     * 初始化uuid的随机数组
     */
    public static List<String> uuidList;

    static {
         uuidList = new ArrayList<>(30000000);

        File uuidFile = new File("/Users/caoxunan/learn-git/elasticsearch-demo/jsonfile/uuidFile");
        FileReader fileReader = null;
        try {

            fileReader = new FileReader(uuidFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String temp = "";
            int i = 0;
            while ((temp = bufferedReader.readLine()) != null) {
                if (i % 1000000 == 0){

                    LOGGER.info("正在初始化随机集合～,已完成 " + i + "条数据。");
                }
                uuidList.add(temp);
                i++;
                if (i == 20000000) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 记录访问的次数
     */
    public static AtomicLong count = new AtomicLong(0);
    /**
     * 当es的搜索的时间超过该数值的时候，会记录到map中
     */
    public static long specificTime = 40;

    /**
     *
     * 临时记录访问次数
     * key:当前第n次访问
     * value：查询ES花费的时间（大于指定时间才会被记录）
     *
     */
    public static Map<Long, String> map = new ConcurrentHashMap<>();

    @Autowired
    private TransportClient client;

    @GetMapping(value = "/")
    public String upload(){
        return "upload";
    }

    @GetMapping(value = "/map/result")
    @ResponseBody
    public ResponseEntity<Map> mapResult(){
        return ResponseEntity.ok(map);
    }

    @PostMapping(value = "/map/reset")
    @ResponseBody
    public ResponseEntity<String> mapReset(){
        map.clear();
        String result = "{\"result\":\"success\"}";
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/doUpload")
    @ResponseBody
    public ResponseEntity<List<String>> doUpload(@RequestParam(value = "file") MultipartFile file){

        // 1 传输用户上传的file文件，得到图片的具体id
        String[] params = getIdBySendFile(file);

        // 2 使用ElasticSearch相关API到索引库中查询结果
        SearchResponse response = client.prepareSearch("imagesearch")
                .setTypes("test")
                .setQuery(QueryBuilders.termsQuery("id", params))                 // Query
                .setSize(60)
                .get();

        // 3 解析响应结果
        List<String> urlList = getUrlListByResponse(response);

        long hooks = response.getTookInMillis();
        LOGGER.info("普通id从ES搜索相关数据共耗时：" + hooks + "ms");

        long specificTime = 50;
        long times = count.incrementAndGet();

        if (hooks > specificTime){
            String record = "普通id搜索，搜索时间为：" + hooks + "ms";
            map.put(times, record);
            LOGGER.info("超出" + specificTime + "ms以上的搜索发生了" + map.size() + "次，普通id搜索花费：" + hooks + "ms");
        }

        return ResponseEntity.ok(urlList);
    }

    private List<String> getUrlListByResponse(SearchResponse response) {

        List<String> urlList = new ArrayList<>();

        SearchHits hits = response.getHits();
        Iterator<SearchHit> iterator = hits.iterator();
        while (iterator.hasNext()) {
            SearchHit hit = iterator.next();
            Map<String, Object> source = hit.getSource();
            String url = (String) source.get("url");
            urlList.add(url);
        }
        return urlList;
    }

    @PostMapping(value = "/uuidDoUpload")
    @ResponseBody
    public ResponseEntity<List<String>> uuidDoUpload(@RequestParam("file") MultipartFile file){

        // 1 传输用户上传的file文件，得到图片的具体id
        String[] params = getUuIdBySendFile(file);

        // 2 使用ElasticSearch相关API到索引库中查询结果
        SearchResponse response = client.prepareSearch("uuidsearch")
                .setTypes("test")
                .setQuery(QueryBuilders.termsQuery("id", params))                 // Query
                .setSize(60)
                .get();

        // 3 解析响应结果
        List<String> urlList = getUrlListByResponse(response);

        long hooks = response.getTookInMillis();
        LOGGER.info("uuid从ES搜索相关数据共耗时：" + hooks + "ms");

        long times = count.incrementAndGet();
        if (hooks > specificTime){
            String record = "uuid搜索，搜索时间为：" + hooks + "ms";
            map.put(times, record);
            LOGGER.info("超出" + specificTime + "ms以上的搜索发生了" + map.size()+ "次，uuid搜索花费：" + hooks + "ms");

        }
        return ResponseEntity.ok(urlList);
    }

    /**
     * 将用户传递的file文件发送给图片搜索引擎，获得S3上的图片uuid
     *
     * @param file 用户上传的文件
     * @return  String[] 图片id的数组集
     */
    private String[] getUuIdBySendFile(MultipartFile file) {
        long startTime = System.currentTimeMillis();
        Set<String> set = new HashSet<>(32);
        int num = 30;
        while (set.size() < num) {
            Random random = new Random();
            int rand = random.nextInt(20000000);
            set.add(uuidList.get(rand));
        }

        String[] params = set.toArray(new String[0]);
        long endTime = System.currentTimeMillis();
        LOGGER.info("通过图片搜索引擎获得图片id共耗时：" + (endTime - startTime) + "ms");
        return params;
    }

    /**
     * 将用户传递的file文件发送给图片搜索引擎，获得S3上的图片id
     *
     * @param file 用户上传的文件
     * @return  String[] 图片id的数组集
     */
    private String[] getIdBySendFile(MultipartFile file) {
        long startTime = System.currentTimeMillis();
        // 创建随机id模拟图片搜索引擎返回具体数据
        Set<String> set = new HashSet<>(32);
        int num = 30;
        while (set.size() < num) {
            Random random = new Random();
            int rand = random.nextInt(50000000);
            set.add(String.valueOf(rand));
        }
        String[] params = set.toArray(new String[0]);
        long endTime = System.currentTimeMillis();
        LOGGER.info("通过图片搜索引擎获得图片id共耗时：" + (endTime - startTime) + "ms");
        return params;
    }

}
