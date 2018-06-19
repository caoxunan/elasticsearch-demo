package com.cxn.elasticsearch.controller;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping(path = "/")
public class DemoController {

    @Autowired
    private TransportClient client;

    @PostMapping("add/imagesearch/test")
    @ResponseBody
    public ResponseEntity add(@RequestParam("id") String id, @RequestParam("url") String url) {

        // 创建json构造器
        try {
            XContentBuilder contentBuilder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("id", id)
                    .field("url", url)
                    .endObject();
            IndexResponse result = client.prepareIndex("imagesearch", "test")
                    .setSource(contentBuilder)
                    .get();
            return new ResponseEntity(result.getId(), HttpStatus.OK);
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

    public ResponseEntity update(String id, String url) {
        UpdateRequest update = new UpdateRequest("imagesearch", "test", id);
        try {
            XContentBuilder contentBuilder = XContentFactory.jsonBuilder()
                    .startObject();
            if (url != null) {
                contentBuilder.field("url", url);
            }
            contentBuilder.endObject();
            update.doc(contentBuilder);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try{
            UpdateResponse result = client.update(update).get();
            return new ResponseEntity(result.getResult().toString(), HttpStatus.OK);
        }catch (Exception e){
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


}
