package com.example.demo.evt;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class Startup {

    //private static String url = "http://pg.bittx.cn:9999/hello";
    private static String url = "http://localhost:9900/hello";

    @Resource
    RestTemplate restTemplate;

    @Resource
    RestTemplate innerRestTemplate;

    @Resource
    OkHttpClient okHttpClient;

    @Resource
    OkHttpClient innerOkHttpCli;


    @EventListener(ApplicationReadyEvent.class)
    public void fire() throws IOException {
        httpTest();
    }

    private void httpTest() throws IOException {
        //Request req = new Request.Builder().url(url).header("User-Agent", "Srv (X11,U,Linux)").build();
        //okHttpClient.networkInterceptors().add(new UserAgentInterceptor("ua"));
        Request req = new Request.Builder().url(url).build();
        okHttpClient.newCall(req).execute();

        // 此处使用了自定义User-Agent的okhttpcli
        innerOkHttpCli.newCall(req).execute();

        restTemplate.headForHeaders(url);

        // 此处使用自定义的User-Agent
        innerRestTemplate.headForHeaders(url);
    }
}
