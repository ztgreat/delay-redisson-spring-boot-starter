# queue-redisson-spring-boot-starter

#### 介绍
redisson的springboot starter简单实现，并基于spring-messaging对redisson的队列和延迟队列进行了实现，\
提供了发送队列消息的操作模板、基于注解的消费者实现。

#### 运行环境及基础架构
java 1.8\
spring 5.0.10\
springboot 2.0.6

#### 安装教程
有私服的可以将jar包放到私服，直接普通的引用即可.
```
<dependency>
    <groupId>com.delay.queue</groupId>
    <artifactId>queue-redisson-spring-boot-starter</artifactId>
    <version>1.0.0.RELEASE</version>
</dependency>
```

#### 使用说明
1.引入starter之后，配置文件新增
```
spring:
  smart-redisson:
    #server-type: single
    #server-address: localhost:6379
    server-type: cluster
    server-address: localhost:7000,localhost:7001,localhost:7002
    password: 123456
```
不加配置默认Redis服务为单实例single、服务连接地址为localhost:6379。\
配置结束之后即可使用通过spring容器使用RedissonClient了。

2.如果需要使用队列，在spring容器注入一个RedissonQueue实例即可，如：
```
@Bean
public RedissonQueue redissonQueue() {
    return new RedissonQueue("test", true, null, messageConverter());
}
```
创建队列的时候可以指定队列名称、是否延迟队列、隔离策略、消息转换器。\
隔离策略主要是应用于如下场景：\
在服务集群模式中，假设A、B、C三台机器，A机器生产的消息只希望自己消费，这就叫做隔离，\
可通过指定隔离策略进行集群的隔离，源码提供了DefaultIsolationStrategy，可根据需要使用。\
消息转换器主要是对消息进行转换以及一些附加处理如增加消息头等

3.向队列发送消息
```
@Autowired
private RedissonTemplate redissonTemplate;

public void test() {
    CarLbsDto carLbsDto = new CarLbsDto();
    carLbsDto.setCid(1);
    carLbsDto.setBusinessType("0");
    carLbsDto.setCity("北京市");
    carLbsDto.setCityId(265);
    carLbsDto.setName("fsfds");
    carLbsDto.setCarNum("156156");
    redissonTemplate.sendWithDelay("test", carLbsDto, 5000);
}
```
4.消费消息\
spring启动类开启@EnableRedisson，编写如下类即可
```
@Configuration
public class RedissonTestApplication {
    @Bean("myMessageConverter")
    public MessageConverter messageConverter() {
        return new MessageConverter() {
            @Override
            public QueueMessage<?> toMessage(Object object, Map<String, Object> headers) throws MessageConversionException {
                //do something you want, eg:
                headers.put("my_header", "my_header_value");
                return QueueMessageBuilder.withPayload(object).headers(headers).build();
            }

            @Override
            public Object fromMessage(RedissonMessage redissonMessage) throws MessageConversionException {
                byte[] payload = redissonMessage.getPayload();
                String payloadStr = new String(payload);
                return JSONObject.parseObject(payloadStr, CarLbsDto.class);
            }
        };
    }

    @RedissonListener(queues = "test", messageConverter = "myMessageConverter")
    public void handler(@Header(value = RedissonHeaders.MESSAGE_ID, required = false) String messageId,
                        @Header(RedissonHeaders.DELIVERY_QUEUE_NAME) String queue,
                        @Header(RedissonHeaders.SEND_TIMESTAMP) long sendTimestamp,
                        @Header(RedissonHeaders.EXPECTED_DELAY_MILLIS) long expectedDelayMillis,
                        @Header(value = "my_header", required = false, defaultValue = "test") String myHeader,
                        @Payload CarLbsDto carLbsDto) {
        System.out.println(messageId);
        System.out.println(queue);
        System.out.println(myHeader);
        long actualDelay = System.currentTimeMillis() - (sendTimestamp + expectedDelayMillis);
        System.out.println("receive " + carLbsDto + ", delayed " + actualDelay + " millis");
    }
} 
```
其中注解可以配置消费的队列、异常处理器、隔离策略、消息转换器
其中隔离策略通常与生产者保持一致
异常处理器可自定义消费发生异常之后如何处理，源码提供了RequeueRedissonListenerErrorHandler，可根据需要使用
消息转换器是把RedissonMessage转换成需要的对象，当然不转换消费方法直接使用RedissonMessage作为参数也是可以的

#### 设计说明/初衷
每个队列均可在定义时指定MessageConverter，如果不指定，则会使用RedissonTemplate中默认的全局MessageConverter。\
每个消费者也可指定MessageConverter，如果不指定，则会尝试在spring容器中中寻找MessageConverter的bean实例，\
还未找到，则使用默认的MessageConverter，默认的MessageConverter进行的操作是将从redis拿到的消息的消息体转换成字符串。\
至于为什么是字符串呢？这还跟Redisson的序列化和反序列化有关。设计的初衷是，为了避免耦合、提升可扩展性，\
序列化和反序列化使用的是fastjson（详见FastJsonCodec编码解码器），并且消息的内容不会包含任何项目或者类相关的信息。\
试想一下，A项目发送消息com.a.TestClass对象都队列，如果是采用java的序列化，那么B项目消费消息的时候也必须严格按照\
com.a.TestClass进行定义类，才能正确的接收到消息的内容，否则反序列化出错。同样json序列化带上类信息时，\
也会存在同样的问题，这不是我们想要的。所以消息的存储完全是与项目、类信息等无关的，仅仅是一个json格式的数据，\
所以消费者读到的数据实际上是一个json格式的字符串，在使用的时候我们要注意到，消费者接收到的消息都是基于json转换而来的，\
如果我们不自定义MessageConverter转换器，那么我们拿到的数据消息体就是一个json字符串，消息头就是一个json对象。