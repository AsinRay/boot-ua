package com.example.demo.conf;

import okhttp3.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 此类用于设置两个不同的Bean 分别用于默认User-Agent请求和定义的User-Agent.
 * 对于新项目来说，这是个demo，很方便使用.
 *
 * 对于已有的项目，可以在发送请求之前，通过设置,header来实现，如下分别给出了okhttp
 * 以及RestTemplate两种设置方式：

   OkHttp可以在构建Request时在header设置User-Agent,无需修改代码
   <pre>
     Request req = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Srv (X11,U,Linux)")
                    .build();
     okHttpClient.newCall(req).execute();
   </pre>

   RestTemplate在header中设置User-Agent时需要构建HttpEntity,并通过HttpEntity来将header信息代入请求，
   RestTemplate的getForObject/getForEntity不支持HttpEntity参数，因此我们需要修改代码，使用exechange
   方法来完成Http的请求.

   <pre>
     String url = "https://www.baidu.com?img=3"
     RestTemplate restTemplate = new RestTemplate();
     HttpHeaders headers = new HttpHeaders();
     headers.set("user-agent",Srv (X11,U,Linux)");
     // 注意几个请求参数
     HttpEntity<String> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null, headers),String.class);
   </pre>

   post 携带请求头，可以利用上面的方式实现；也可直接使用postForObject/postForEntity来完成Http的请求

 *
 *
 *
 * Note: 其中的参数可以通过配置文件加载，在此不做过多说明，有需要请自行实现,
 * 如下给出一个示例：
    <pre>
    @Value("${ok.http.write-timeout}")
    private Integer writeTimeout;
    </pre>

 *
 * @see org.springframework.web.client.RestTemplate
 * @see org.springframework.http.HttpEntity
 * @author Asin Ray
 */
@Configuration
public class HttpConf {

    @Bean
    public RestTemplate restTemplate(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000);
        factory.setReadTimeout(60000);
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }

    @Bean
    public RestTemplate innerRestTemplate(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000);
        factory.setReadTimeout(60000);

        ClientHttpRequestInterceptor interceptor = (httpRequest, bytes, execution) -> {
            httpRequest.getHeaders().set("user-agent", "Srv (X11; U; Linux)");
            return execution.execute(httpRequest, bytes);
        };
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add(interceptor);
        return restTemplate;
    }

    @Bean
    public ConnectionPool okHttpCP(){
        return new ConnectionPool(32,120,TimeUnit.SECONDS);
    }

    @Bean
    public OkHttpClient okHttpClient(UserAgentInterceptor userAgentInterceptor){
        return new OkHttpClient.Builder()
                //.sslSocketFactory(sslSocketFactory(), x509TrustManager())
                // 是否开启缓存
                .retryOnConnectionFailure(false)
                .connectionPool(okHttpCP())
                .connectTimeout(60000, TimeUnit.SECONDS)
                .readTimeout(60000, TimeUnit.SECONDS)
                .writeTimeout(60000,TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public OkHttpClient innerOkHttpCli(UserAgentInterceptor userAgentInterceptor){

        return new OkHttpClient.Builder()
                //.sslSocketFactory(sslSocketFactory(), x509TrustManager())
                .retryOnConnectionFailure(false)
                .connectionPool(okHttpCP())
                .connectTimeout(60000, TimeUnit.SECONDS)
                .readTimeout(60000, TimeUnit.SECONDS)
                .writeTimeout(60000,TimeUnit.SECONDS)
                //.hostnameVerifier((hostname, session) -> true)
                // .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8888)))
                // 拦截器
                .addInterceptor(userAgentInterceptor)
                .build();
    }

    @Bean
    public UserAgentInterceptor okHttpClientUserAgentInterceptor(){
        return new UserAgentInterceptor("Srv (X11; U; Linux)");
    }

    public static class UserAgentInterceptor implements Interceptor {
        private final String userAgent;
        public UserAgentInterceptor(String userAgent) {
            this.userAgent = userAgent;
        }
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                    .header("User-Agent", userAgent)
                    .build();
            return chain.proceed(requestWithUserAgent);
        }
    }
}
