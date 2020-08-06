# OpenFeign - Declarative REST Client

Feign是一种声明式、模板化的HTTP客户端。在Spring Cloud中使用Feign，可以做到使用HTTP请求访问远程服务，就像调用本地方法一样，开发者完全感知不到这是在调用远程方法，更感知不到在访问HTTP请求。使用Feign只需创建一个接口并添加对应的注解，例如：@FeignClient注解。Feign有可插拔的注解，包括Feign注解和JAX-RS注解。Feign也支持编码器和解码器，Spring Cloud Open Feign是对Feign进行增强并支持Spring MVC注解，可以像Spring Web一样使用HttpMessageConverters等。

- https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/

**主要功能：**

1. 可插拔的注解支持，包括Feign注解和JAX-RS注解。
2. 支持可插拔的HTTP编码器和解码器（Gson，Jackson，Sax，JAXB，JAX-RS，SOAP）。
3. 支持Hystrix和它的Fallback。
4. 支持Ribbon的负载均衡。
5. 支持HTTP请求和响应的压缩。
6. 灵活的配置：基于 name 粒度进行配置。
7. 支持多种客户端：JDK URLConnection、apache httpclient、okhttp，ribbon）
8. 支持日志
9. 支持错误重试
10. url支持占位符
11. 可以不依赖注册中心独立运行

## 快速开始

**1. 在你的 Spring Boot 项目中添加以下依赖**

```
<dependency>
  <groupId>com.github.zhycn</groupId>
  <artifactId>muyie-integration-openfeign</artifactId>
  <version>{latest version}</version>
</dependency>
```

或者：

```
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**2. 在启动类上添加@EnableFeignClients注解**

@EnableFeignClients声明该项目是Feign客户端，并扫描对应的FeignClient。

```
@SpringBootApplication
@EnableFeignClients
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
```

**3. 定义Feign客户端**

OpenFeign客户端用于调用相关服务，用法和Spring Web一样。

```
@FeignClient("stores")
public interface StoreClient {

  @RequestMapping(method = RequestMethod.GET, value = "/stores")
  List<Store> getStores();

  @RequestMapping(method = RequestMethod.GET, value = "/stores")
  Page<Store> getStores(Pageable pageable);

  @RequestMapping(method = RequestMethod.POST, value = "/stores/{storeId}", consumes = "application/json")
  Store update(@PathVariable("storeId") Long storeId, Store store);

}
```

原生的Feign也提供了相关注解：

- @RequestLine：定义与之通信的动词和URI的路径
- @QueryMap：查询参数集合
- @Param：参数定义
- @Headers：头信息
- @HeaderMap：头信息集合
- @Body：请求报文体

两种写法的对比如下：

```
//Spring
@RequestMapping(method = RequestMethod.GET, value = "auth")
boolean isToken(@RequestHeader("Authorization") String token);

//OpenFeign
@RequestLine("GET /auth")
@Headers({"Authorization: {token}", "Accept: application/hal+json"})
boolean isValid(@Param("token") String token);
```

**4. 示例**

定义好Feign客户端后，使用就变得十分简单了。

```
@Component
public class TestService {

  @Autowired
  private StoreClient storeClient;

  public void test() {
    storeClient.getStores();
  }
}
```

## 使用详解

Feign的基本用法非常简单，但不同使用场景仍需要做一些配置。

**1. 简单的HTTP调用**

定义一个简单的外部接口，设置URL的值，可以通过占位符的方式通过配置文件传入。

```
@FeignClient(name = "myFeignClient", url = "http://127.0.0.1:8001")
public interface MyFeignClient {

  @RequestMapping(method = RequestMethod.GET, value = "/participate")
  String getCategorys(@RequestParam Map<String, Object> params);
}
```

如果不使用@FeignClient注解，也可以自定义创建对象（配置更加灵活）：

```
@Bean
MyFeignClient myFeignClient() {
  return Feign.builder()
    .requestInterceptor(requestInterceptor()) // 可以自定义拦截器或请求头参数，使用注解则要通过configuration配置。
    .target(MyFeignClient.class, "http://127.0.0.1:8001");
}
```

**2. 文件上传**

如果想使用文件上传接口或者post的x-www-form-urlencoded接口，需要做如下配置：

```
@Bean
@ConditionalOnMissingBean
public Encoder multipartFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
  return new SpringFormEncoder(new SpringEncoder(messageConverters));
}
```

定义文件上传接口：

```
@RequestMapping(value = {"/v1/upload"}, method = {RequestMethod.POST}, consumes = {
    MediaType.MULTIPART_FORM_DATA_VALUE})
ReturnResult<ImageVO> uploadFile(@RequestPart(value = "file") MultipartFile file,
    @RequestParam(value = "bucketName", required = false) String bucketName);
```

**3. 使用HttpClient连接池**

_http 的背景原理_

a. 两台服务器建立 http 连接的过程是很复杂的一个过程，涉及到多个数据包的交换，并且也很耗时间。

b. Http 连接需要的 3 次握手 4 次分手开销很大，这一开销对于大量的比较小的 http 消息来说更大。

_优化解决方案_

a. 如果我们直接采用 http 连接池，节约了大量的 3 次握手 4 次分手；这样能大大提升吞
吐率。

b. feign 的 http 客户端支持 3 种框架；HttpURLConnection、httpclient、okhttp；默认是
HttpURLConnection。（Spring Cloud OpenFeign默认采用了httpclient）

c. 传统的 HttpURLConnection 是 JDK 自带的，并不支持连接池，如果要实现连接池的
机制，还需要自己来管理连接对象。对于网络请求这种底层相对复杂的操作，如果有可用的
其他方案，也没有必要自己去管理连接对象。

d. HttpClient 相比传统 JDK 自带的 HttpURLConnection，它封装了访问 http 的请求头，
参数，内容体，响应等等；它不仅使客户端发送 HTTP 请求变得容易，而且也方便了开发人
员测试接口（基于 Http 协议的），即提高了开发的效率，也方便提高代码的健壮性；另外
高并发大量的请求网络的时候，还是用连接池提升吞吐量。

```
## 使用Apache HttpClient（默认）
feign.httpclient.enabled=true

## 使用OkHttp
## 底层代码实现参考：OkHttpFeignLoadBalancerConfiguration 和 OkHttpFeignConfiguration。
feign.httpclient.enabled=true

## 以上二选一，两者都为false则使用JDK自带的Http连接。

## httpclient连接池的默认配置如下：
## 一般情况下，使用默认配置即可，特殊场景下，连接超时时间可以设置大一点。
## Apache HttpClient 和 OkHttp 都支持以下连接池配置。
feign.httpclient.connection-timeout=2000
feign.httpclient.connection-timer-repeat=3000
feign.httpclient.disable-ssl-validation=false
feign.httpclient.follow-redirects=true
feign.httpclient.max-connections=200
feign.httpclient.max-connections-per-route=50
feign.httpclient.time-to-live=900
feign.httpclient.time-to-live-unit=seconds
```

**4. 日志配置**

Feign提供了日志打印功能，我们在项目中可以通过配置来调整日志级别，从而了解Feign中http请求的细节 ，也就是说feign提供的日志功能可以对接口的调用情况进行监控和输出。

日志级别： 

- NONE: 默认的，不显示任何日志
- BASIC：仅记录请求方法、URL、响应状态码以及执行时间
- HEADERS：除了BASIC中定义的信息以外，还有请求和响应的头信息
- FULL：除了HEADERS中定义的信息之外，还有请求和响应的正文及元数据

(1) 通过Bean的方式配置（不推荐使用，原因是不够灵活）

```
@Bean
Logger.Level feignLoggerLevel(){
  return Logger.Level.FULL; // 设置日志
}
```

(2) 通过配置文件的方式配置（推荐）

```
# 全局设置
feign.client.config.default.logger-level=full

# 针对单个@FeignClient("stores")设置日志级别
feign.client.config.stores.logger-level=full
```

在设置了Feign的日志级别后，还需要开启对应的debug模式才能看到打印日志：

```
# com.example.demo 指定的包名，也可以具体到类
logging.level.com.example.demo=debug
```

**5. 压缩**

为了提高传输效率，可开启压缩，以下是两种压缩方式：

```
#-----------------------------feign gzip
# 配置请求 GZIP 压缩
feign.compression.request.enabled=true
# 配置响应 GZIP 压缩
feign.compression.response.enabled=true
# 配置压缩支持的 MIME TYPE
feign.compression.request.mime-types=text/xml,application/xml,application/json
# 配置压缩数据大小的最小阀值，默认 2048
feign.compression.request.min-request-size=2048

#-----------------------------spring boot gzip
# 是否启用压缩
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain
```


**6. 启用Hystrix熔断降级**

Feign可以很好的配合Spring Cloud生态使用，如：Hystrix和Ribbon。

添加Hystrix的属性配置：

```
feign:
  hystrix: 
    enabled: true

hystrix:
  command:
    default:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 15000
  threadpool:
    default:
      coreSize: 40
      maximumSize: 100
      maxQueueSize: 100
```

添加降级策略：

```
@Component
public class MyFeignClientFallback implements MyFeignClient {

  @Override
  public ReturnResult<ImageVO> uploadFile(MultipartFile file, String bucketName) {
    return new ReturnResult<>(5001);
  }
}
```

如果想要处理熔断的具体原因，可以做如下更新：

```
@Component
public class MyFeignClientFallback implements FallbackFactory<MyFeignClient> {

  @Override
  public MyFeignClient create(final Throwable cause) {
    return new MyFeignClient() {
      @Override
      public ReturnResult<ImageVO> uploadFile(MultipartFile file, String bucketName) {
        // 处理异常 -> cause
        return new ReturnResult<>(5001);
      }
    };
  }
}
```

更新@FeignClient代码：

```
@FeignClient(name = "myFeignClient", url = "http://127.0.0.1:8001", fallback = MyFeignClientFallback.class)
public interface MyFeignClient {

  @RequestMapping(method = RequestMethod.GET, value = "/participate")
  String getCategorys(@RequestParam Map<String, Object> params);
}
```

**7. 配合负载均衡使用**

如果项目中使用了Spring Cloud注册中心和Ribbon，则需要注意@FeignClient的配置参数。原先使用serviceId来发现服务，现已改用name或value来代替了serviceId作用服务发现，其他不变。

假如要配置两个相同name的服务，使用@FeignClient的方式就办不到，需要通过Feign Builder API手动创建接口代理，而不是通过注解：

```
// FeignClients的很多默认配置，如果修改可参考FeignClientsConfiguration配置类
@Import(FeignClientsConfiguration.class)
class FooController {

  private FooClient fooClient;

  private FooClient adminClient;

  @Autowired
  public FooController(Decoder decoder, Encoder encoder, Client client) {
    this.fooClient = Feign.builder().client(client).encoder(encoder).decoder(decoder)
        .contract(new SpringMvcContract())
        .requestInterceptor(new BasicAuthRequestInterceptor("user", "user"))
        .target(FooClient.class, "http://PROD-SVC");
    this.adminClient = Feign.builder().client(client).encoder(encoder).decoder(decoder)
        .contract(new SpringMvcContract())
        .requestInterceptor(new BasicAuthRequestInterceptor("admin", "admin"))
        .target(FooClient.class, "http://PROD-SVC");
  }
}
```

构造Feign时需要的encoder, decoder, client都可以从容器中注入，这样构造出来的feign代理对象与我们使用@FeignClient注解功能上是完全一样的。

**8. 自定义配置和拦截器**

Feign配置类，可以自定义Feign的Encoder、Decoder、LogLevel、Contract。

实例：自定义configuration配置类，简单的定义一个自己的Decoder，该Decoder配合decod404=true使用；当服务调用抛出404错误时，将自动调用自定义的Decoder，输出一个简单的字符串。

(1) 编写自定义的Decoder类MyDecoder。

```
public class MyDecoder implements Decoder {

  // 当调用服务报404时，则会输出自定义信息。
  public Object decode(Response response, Type type)
      throws IOException, DecodeException, FeignException {
    return "MyDecode response=" + response;
  }

}
```

(2) 继承 RequestInterceptor 类

这种方式声明的拦截器是全局的，也就是所有的 FeignClient 发出的请求都会走这个拦截器。

```
@Configuration
public class DemoConfiguration implements RequestInterceptor {

  public void apply(RequestTemplate template) {
    // 拦截器的处理逻辑，例如添加统一请求头信息
  }

  // 自定义其他配置
  @Bean
  public Decoder myDecoder() {
    return new MyDecoder();
  }

}
```

(3) 指定配置和拦截器

在某一些情况下，我们只需要拦截部分特定的URL，也就是为每一个@FeignClient单独设置拦截器，那么你可以使用这种方式通过 FeignClient 中的 configuration 属性来设置。

```
public class DemoConfiguration {

  // 定义拦截器
  @Bean
  public RequestInterceptor demoRequestInterceptor() {
    return template -> {
      // 拦截器的处理逻辑
    };
  }

  // 自定义其他配置
  @Bean
  public Decoder myDecoder() {
    return new MyDecoder();
  }

}
```

(4) 在FeignClient中指定配置类

```
// 通过configuration属性加载拦截器和配置
@FeignClient(name = "myFeignClient", url = "http://127.0.0.1:8001", configuration = DemoConfiguration.class)
public interface MyFeignClient {

  @RequestMapping(method = RequestMethod.GET, value = "/participate")
  String getCategorys(@RequestParam Map<String, Object> params);
}
```

**9. decode404**

在没有熔断处理的情况下，调用Feign服务失败会抛出FeignException，这时返回状态码是500。设置decode404=true，可通过设置configuration去配置自定义decode。通过源码得知，默认情况下使用FeignClientsConfiguration类，其中Decoder默认使用SpringDecoder。FeignClientsConfiguration部分源代码如下：

```
@Bean
@ConditionalOnMissingBean
public Decoder feignDecoder() {
  return new OptionalDecoder(
      new ResponseEntityDecoder(new SpringDecoder(this.messageConverters)));
}
```

**10. path**

path属性定义当前@FeignClient的统一前缀，这样方便在该@FeignClient中的@RequestMapping中书写value值。假如存在一系列的用户管理服务，如下：

```
/app/service/userManager/get
/app/service/userManager/insert
/app/service/userManager/update
/app/service/userManager/delete
```

每次都在@RequestMapping注解中编写全服务名称，是不是有点麻烦。因此可以设置FeignClient的path路径为`/app/service/userManager`，简化@RequestMapping的编写。
