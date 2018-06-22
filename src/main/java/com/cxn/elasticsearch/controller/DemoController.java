package com.cxn.elasticsearch.controller;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.profile.ProfileShardResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Controller
@RequestMapping(path = "/")
public class DemoController {

    final static Logger logger = LoggerFactory.getLogger(DemoController.class);
    public static int count = 0;
    public static List<String> uuidList = null;
    //static {
    //     uuidList = new ArrayList<>(30000000);
    //
    //    File uuidFile = new File("/Users/caoxunan/learn-git/elasticsearch-demo/jsonfile/uuidFile");
    //    FileReader fileReader = null;
    //    try {
    //
    //        fileReader = new FileReader(uuidFile);
    //        BufferedReader bufferedReader = new BufferedReader(fileReader);
    //
    //        String temp = "";
    //        int i = 0;
    //        while ((temp = bufferedReader.readLine()) != null) {
    //            if (i % 1000000 == 0){
    //
    //                logger.info("正在初始化随机集合～,已完成 " + i + "条数据。");
    //            }
    //            uuidList.add(temp);
    //            i++;
    //            if (i == 20000000) {
    //                break;
    //            }
    //        }
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //}

    @Autowired
    private TransportClient client;

    @PostMapping("add/imagesearch/test")
    @ResponseBody
    public ResponseEntity add(@RequestParam("id") String id, @RequestParam("url") String url) {

        // 创建json构造器
        try {
            XContentBuilder contentBuilder = jsonBuilder()
                    .startObject()
                    .field("id", id)
                    .field("url", url)
                    .endObject();
            String jsonBody = contentBuilder.string();
            System.out.println("JsonContext：" + jsonBody);
            IndexResponse response = client.prepareIndex("imagesearch", "test")
                    .setSource(contentBuilder)
                    .get();

            // Index name
            String _index = response.getIndex();
            // Type name
            String _type = response.getType();
            // Document ID (generated or not)
            String _id = response.getId();
            // Version (if it's the first time you index this document, you will get: 1)
            long _version = response.getVersion();
            // status has stored current instance statement.
            RestStatus status = response.status();

            return new ResponseEntity(response.getId(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("delete/imagesearch/test")
    @ResponseBody
    public ResponseEntity delete(@RequestParam(name = "id") String id) {
        DeleteResponse result = client.prepareDelete("imagesearch", "test", id).get();
        return new ResponseEntity(result.getResult().toString(), HttpStatus.OK);
    }

    @DeleteMapping("deleteByQuery/imagesearch/test/demo")
    @ResponseBody
    public ResponseEntity deleteByQuery() {

        // 方法一
        BulkByScrollResponse response =
                DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                        // execute the query
                        .filter(QueryBuilders.matchQuery("gender", "male"))
                        // set index
                        .source("persons")
                        // execute the operation
                        .get();
        // number of deleted documents
        long deleted = response.getDeleted();

        // 方法二
        // 如果该操作可能执行的时间很长的话，可以使用异步的方式执行，使用execute代替get，然后提供一个监听器listener
        DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                .filter(QueryBuilders.matchQuery("gender", "male"))
                .source("persons")
                .execute(new ActionListener<BulkByScrollResponse>() {
                    @Override
                    public void onResponse(BulkByScrollResponse response) {
                        long deleted = response.getDeleted();
                        // do something
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Handle the exception
                    }
                });

        return new ResponseEntity(deleted, HttpStatus.OK);
    }

    @PutMapping("update/imagesearch/test")
    @ResponseBody
    public ResponseEntity update(@RequestParam("id") String id, @RequestParam("url") String url) {

        // 更多API参见 https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-docs-update.html

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("imagesearch");
        updateRequest.type("test");
        updateRequest.id(id);

        try {
            XContentBuilder contentBuilder = jsonBuilder()
                    .startObject();
            if (url != null) {
                contentBuilder.field("url", url);
            }
            contentBuilder.endObject();
            updateRequest.doc(contentBuilder);

            UpdateResponse result = client.update(updateRequest).get();
            return new ResponseEntity(result.getResult().toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @GetMapping("get/imagesearch/test")
    @ResponseBody
    public ResponseEntity get(@RequestParam(name = "id", defaultValue = "") String id) {

        if (id.isEmpty()) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        GetResponse result = client.prepareGet("imagesearch", "test", id).get();

        if (!result.isExists()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(result.getSource(), HttpStatus.OK);

    }

    @GetMapping("multiGet/demo")
    @ResponseBody
    public ResponseEntity multiGet() {
        MultiGetResponse multiGetItemResponses = client.prepareMultiGet()
                .add("twitter", "tweet", "1")
                .add("twitter", "tweet", "2", "3", "4")
                .add("another", "type", "foo")
                .get();

        for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
            GetResponse response = itemResponse.getResponse();
            if (response.isExists()) {
                String json = response.getSourceAsString();
                // 根据需要封装结果
            }
        }
        return null;
    }

    @PutMapping("bulkAPI/demo")
    @ResponseBody
    public ResponseEntity bulkAPI() {

        // see it for more
        // https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-docs-bulk-processor.html

        BulkRequestBuilder bulkRequest = client.prepareBulk();

        try {
            // either use client#prepare, or use Requests# to directly build index/delete requests
            bulkRequest.add(client.prepareIndex("twitter", "tweet", "1")
                    .setSource(jsonBuilder()
                            .startObject()
                            .field("user", "kimchy")
                            .field("postDate", new Date())
                            .field("message", "trying out Elasticsearch")
                            .endObject()
                    )
            );

            bulkRequest.add(client.prepareIndex("twitter", "tweet", "2")
                    .setSource(jsonBuilder()
                            .startObject()
                            .field("user", "kimchy")
                            .field("postDate", new Date())
                            .field("message", "another post")
                            .endObject()
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            // process failures by iterating through each bulk response item
        }
        return null;
    }

    @GetMapping("searchApi/demo")
    @ResponseBody
    public ResponseEntity searchApi() {

        long start = System.currentTimeMillis();

        // 创建随机id模拟图片搜索引擎返回具体数据
        Set<String> set = new HashSet<>(32);
        int num = 30;
        while (set.size() < num) {
            Random random = new Random();
            int rand = random.nextInt(50000000);
            set.add(String.valueOf(rand));
        }
        String[] params = set.toArray(new String[0]);
        long end = System.currentTimeMillis();
        //String arr = Arrays.toString(params);
        //System.out.println("随机参数数组：" + arr);
        long initTime = end -start;
        SearchResponse response = client.prepareSearch("imagesearch")
                .setTypes("test")
                // .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termsQuery("id", params))                 // Query
                // .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
                .setSize(60)
                // .setExplain(true)
                .get();
        //SearchHits hits = response.getHits();
        //
        //Iterator<SearchHit> iterator = hits.iterator();
        //while (iterator.hasNext()) {
        //    SearchHit hit = iterator.next();
        //
        //    Map<String, Object> source = hit.getSource();
        //
        //    String url = (String) source.get("url");
        //    String id = (String) source.get("id");
        //    System.out.println(id + "::::::" + url);
        //
        //}
        long hooks = response.getTookInMillis();
        long nbHits = response.getHits().getTotalHits();
        if (hooks > 50){
            count ++;
            logger.info("超出50ms以上的搜索发生了" + count+ "次，普通id搜索花费：" + hooks + "ms");
        }

        return null;
    }

    @GetMapping("uuidSearchApi/demo")
    @ResponseBody
    public ResponseEntity uuidSearchApi() throws Exception {

        // 创建随机id模拟图片搜索引擎返回具体数据
        long start = System.currentTimeMillis();
        Set<String> set = new HashSet<>(32);
        int num = 30;
        while (set.size() < num) {
            Random random = new Random();
            int rand = random.nextInt(20000000);
            set.add(uuidList.get(rand));
        }

        String[] params = set.toArray(new String[0]);
        long end = System.currentTimeMillis();
        long initTime = end - start ;

        //String result = Arrays.toString(params);
        //System.out.println("rand uuid :" + result);


        SearchResponse response = client.prepareSearch("uuidsearch")
                .setTypes("test")
                .setQuery(QueryBuilders.termsQuery("id", params))                 // Query
                .setSize(60)
                .get();
        //SearchHits hits = response.getHits();

        //Iterator<SearchHit> iterator = hits.iterator();
        //while (iterator.hasNext()) {
        //    SearchHit hit = iterator.next();
        //
        //    Map<String, Object> source = hit.getSource();
        //
        //    String url = (String) source.get("url");
        //    String id = (String) source.get("id");
        //    System.out.println(id + "::::::" + url);
        //
        //}
        long hooks = response.getTookInMillis();
        long nbHits = response.getHits().getTotalHits();
        if (hooks > 50){
            count++;
            logger.info("超出50ms以上的搜索发生了" + count+ "次，uuid搜索花费：" + hooks + "ms");
        }
        return null;
    }


    @GetMapping("multiSearch/demo")
    @ResponseBody
    public ResponseEntity multiSearchApi() {

        //SearchRequestBuilder srb1 = client
        //        .prepareSearch().setQuery(QueryBuilders.queryStringQuery("image"))
        //        // 设置返回的hit数，默认10
        //        .setSize(10);
        SearchRequestBuilder srb2 = client
                .prepareSearch().setQuery(QueryBuilders.matchQuery("id", "123456"))
                .setSize(10);
        SearchRequestBuilder srb3 = client
                .prepareSearch().setQuery(QueryBuilders.termsQuery("id", "123456", "1234", "5678"))
                .setSize(10);

        MultiSearchResponse sr = client.prepareMultiSearch()
                // .add(srb1)
                .add(srb2)
                .add(srb3)
                .get();

        // You will get all individual responses from MultiSearchResponse#getResponses()
        long nbHits = 0;
        long hooks = 0;
        for (MultiSearchResponse.Item item : sr.getResponses()) {
            SearchResponse response = item.getResponse();
            SearchHits hits = response.getHits();
            Iterator<SearchHit> iterator = hits.iterator();
            while (iterator.hasNext()) {
                SearchHit next = iterator.next();
                Map<String, Object> source = next.getSource();
                String url = (String) source.get("url");
                String id = (String) source.get("id");
                System.out.println(id + "::::::" + url);
            }
            nbHits += response.getHits().getTotalHits();
            hooks += response.getTookInMillis();
            ;

        }
        System.out.println("total hits ：" + nbHits);
        System.out.println("search cost :" + hooks);
        return null;
    }


}
