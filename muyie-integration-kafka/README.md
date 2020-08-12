# Apache Kafka

Apache Kafka是分布式发布-订阅消息系统。它最初由LinkedIn公司开发，之后成为Apache项目的一部分。Kafka是一种快速、可扩展的、设计内在就是分布式的，分区的和可复制的提交日志服务。

http://kafka.apache.org/

Apache Kafka与传统消息系统相比，有以下不同：

1. 它被设计为一个分布式系统，易于向外扩展；
2. 它同时为发布和订阅提供高吞吐量；
3. 它支持多订阅者，当失败时能自动平衡消费者；
4. 它将消息持久化到磁盘，因此可用于批量消费，例如ETL，以及实时应用程序。

## 快速开始

**1. 在你的 Spring Boot 项目中添加以下依赖**

```
<dependency>
  <groupId>com.github.zhycn</groupId>
  <artifactId>muyie-integration-kafka</artifactId>
  <version>{latest version}</version>
</dependency>
```

或者：

```
<dependency>
  <groupId>org.springframework.kafka</groupId>
  <artifactId>spring-kafka</artifactId>
</dependency>
```

### 生产者

生产者发送消息：

```
@Component
public class MessageSender {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  private String topic = "test-topic";

  public void send(String message) {
    kafkaTemplate.send(topic, message);
  }

}
```

生产者配置参数：

```
## kafka settings
# 指定kafka server的地址，集群配置用逗号(,)隔开
spring.kafka.bootstrap-servers=127.0.0.1:9092

## kafka producer
# producer要求leader在考虑完成请求之前收到的确认数，用于控制发送记录在服务端的持久化，其值可以为如下：
# acks = 0 如果设置为0，则生产者将不会等待来自服务器的任何确认，该记录将立即添加到套接字缓冲区并视为已发送。在这种情况下，无法保证服务器已收到记录，并且重试配置将不会生效（因为客户端通常不会知道任何故障），为每条记录返回的偏移量始终设置为-1。
#acks = 1 这意味着leader会将记录写入其本地日志，但无需等待所有副本服务器的完全确认即可做出回应，在这种情况下，如果leader在确认记录后立即失败，但在将数据复制到所有的副本服务器之前，则记录将会丢失。
#acks = all 这意味着leader将等待完整的同步副本集以确认记录，这保证了只要至少一个同步副本服务器仍然存活，记录就不会丢失，这是最强有力的保证，这相当于acks = -1的设置。
# 可以设置的值为：all, -1, 0, 1
spring.kafka.producer.acks=1

# 写入失败时，重试次数。当leader节点失效，一个repli节点会替代成为leader节点，此时可能出现写入失败，
# 当retris为0时，producer不会重复。retirs重发，此时repli节点完全成为leader节点，不会产生消息丢失。
spring.kafka.producer.retries=0

# 每次批量发送消息的数量，producer积累到一定数据，一次发送
spring.kafka.producer.batch-size=65536

# producer积累数据一次发送，缓存大小达到时就发送数据
spring.kafka.producer.buffer-memory=67108864

# 指定消息key和消息体的序列化方式
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

### 消费者

消费者处理消息：

```
@Component
public class MessageHandler {

  // 方式一：只接收消息，支持多个topic
  @KafkaListener(topics = {"test-topic"})
  public void handle(String message) {

  }

  // 方式二：接收消息记录，包括消息元数据。支持多个topic
  @KafkaListener(topics = {"test-topic", "test-topic2"})
  public void handle(ConsumerRecord<String, String> record) {

  }

}
```

消费者配置参数：

```
## kafka settings
# 指定kafka server的地址，集群配置用逗号(,)隔开
spring.kafka.bootstrap-servers=127.0.0.1:9092

## kafka consumer
# 指定消息者所属分组
spring.kafka.consumer.group-id=testGroup

# 消费者读取数据的策略，可选值：
# earliest：当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费。
# latest：当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据。
# none：topic各分区都存在已提交的offset时，从offset后开始消费；只要有一个分区不存在已提交的offset，则抛出异常。
spring.kafka.consumer.auto-offset-reset=earliest

# 设置自动提交offset
spring.kafka.consumer.enable-auto-commit=true

# 消费者自动提交的间隔时间（以毫秒为单位），默认值为5000。
spring.kafka.consumer.auto-commit-interval=10000

# 指定消息key和消息体的反序列化方式
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer

# 服务器在回答获取请求之前将阻塞的最长时间
spring.kafka.consumer.fetch-max-wait=30000

# 一次调用poll()操作时返回的最大记录数
spring.kafka.consumer.max-poll-records=5

# 调用poll()操作的超时时间
spring.kafka.listener.poll-timeout=600000
```