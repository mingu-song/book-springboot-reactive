# 스프링 부트 실전 활용 마스터
* 원본 깃헙 :: https://github.com/onlybooks/spring-boot-reactive
* spring boot 2.6.4 / gradle build / java 11 로 변경하여 진행
---
## 2장 스프링 부트를 활용한 데이터 엑세스
* 관계형 데이터베이스에 대한 리액티브 스트림 지원은 아직 1.0 이 아님
  * https://r2dbc.io/
* embedded mongo 사용 포기
  * 당최 어떻게 설정해야 초기 init까지 동작하는지 모르겠음
  * 이런저런 에러가 많이 발생하여, 그냥 일반설정으로 사용하기로 함
* mongo db
  * `docker-compose.yml` 로 설치
```yaml
version: "3"
services:
  mongodb:
    image: mongo:5.0
    restart: always
    container_name: mongo5
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: root
      MONGO_INITDB_DATABASE: reactive
    volumes:
      - C:\dev\docker\mongo\data:/data/db
```
* `application.properties` 설정
```properties
# local ip
spring.data.mongodb.host=192.168.35.9
spring.data.mongodb.port=27017
spring.data.mongodb.database=reactive
spring.data.mongodb.username=root
spring.data.mongodb.password=root
spring.data.mongodb.authentication-database=admin
```
* `InitDatabase.java`
  * 초기 데이터 설정을 repository로 설정
```java
@Component
public class InitDatabase {
    private final Logger log = LoggerFactory.getLogger(InitDatabase.class);

    private final List<Item> itemList =
            List.of(new Item("Alf alarm clock", 19.99), new Item("Smurf TV tray", 24.99));

    @Bean
    public ApplicationRunner initItems(ItemRepository itemRepository) {
        return args -> {
            itemRepository
                    .deleteAll()
                    .thenMany(Flux.just(itemList).flatMap(itemRepository::saveAll))
                    .thenMany(itemRepository.findAll())
                    .subscribe(item -> log.info(item.toString()));
        };
    }
}
```
* map vs flatMap 비교
  * map : '이것'을 '저것'으로
  * flatMap : '이것'의 스트림을 다른 크기로 된 '저것'의 스트림으로 
* `ReactiveQueryByExampleExecutor` 사용법은 처음봄
---
## 3장 스프링 부트 개발자 도구
* devTools 를 통한 liveReload 가 가능하지만 개인적으로 선호하지 않아 pass
* `logging.level.web=DEBUG` property 설정은 유용한 듯.. 쓸곳을 찾아보자
* `Hooks.onOperatorDebug(); ~~ Hooks.resetOnOperatorDebug();` 개발환경에서 사용 (운영은 그냥 `log()`)
* `BlockHound` 매우 좋네? 의도하지 않은곳의 체크만 잘 처리하면.. (`allowBlockingCallsInside()`)
---
## 4장 스프링 부트 테스트
* 각 클래스 우클릭 -> Go to -> Test 를 통한 스탠다드 방법으로 생성하도록 함
* 머지 갑자기 코드 구조가 바뀜 InventoryService 가 언제 이렇게 ... ;;
* `as(StepVerifier::create)`
  * 리액티브 테스트 케이스는 처음이라 잘 익혀둬야겠음
* 슬라이스 테스트에 사용되는 `@...Test`
  * `@ExtendWith(SpringExtension.class)`를 포함하고 있음
* `blockhound-junit-platform`
  * gradle 에러가 발생하여 모든 테스트가 실패함. 다음에 찾아보는걸로...
```java
org.gradle.api.internal.tasks.testing.TestSuiteExecutionException: Could not complete execution for Gradle Test Executor 2.
	at org.gradle.api.internal.tasks.testing.SuiteTestClassProcessor.stop(SuiteTestClassProcessor.java:63)
	at java.base@11/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
...
Caused by: java.lang.NoClassDefFoundError: org/junit/platform/launcher/TestExecutionListener
    at java.base/java.lang.ClassLoader.defineClass1(Native Method)
    at java.base/java.lang.ClassLoader.defineClass(ClassLoader.java:1016)
    at java.base/java.security.SecureClassLoader.defineClass(SecureClassLoader.java:174)
    at java.base/jdk.internal.loader.BuiltinClassLoader.defineClass(BuiltinClassLoader.java:801)
...
Caused by: java.lang.ClassNotFoundException: org.junit.platform.launcher.TestExecutionListener
    at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:582)
    at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:178)
    at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:521)
    ... 48 more
```
---
## 5장 스프링 부트 운영
#### 어플리케이션 배포
* gradle -> tasks -> build -> bootJar 로 jar 파일생성
* 위치는 `build/libs/book-reactive-0.0.1-SNAPSHOT.jar`
* jar 파일 내장된 계층 확인
```shell
C:\book-reactive\build\libs>java -Djarmode=layertools -jar book-reactive-0.0.1-SNAPSHOT.jar list
dependencies
spring-boot-loader
snapshot-dependencies
application
```
* Dockerfile 은 openjdk11 사용
* 도커빌드 `docker build . --tag reactive-springboot`
* 생성 이미지 확인
```shell
C:\book-reactive>docker images
REPOSITORY            TAG       IMAGE ID       CREATED          SIZE
reactive-springboot   latest    d53ce1847a7c   37 seconds ago   469MB
mongo                 5.0       2785fff65421   2 days ago       698MB
```
* 실행 `docker run --name springboot -it -p 9000:9000 reactive-springboot`
* 페이키토 빌드팩 기반 이미지 빌드 생략 ㅋ
#### 운영 어플리케이션 관리
* 스프링 부트 버전으로 인한 차이점이 많아 필요한 것들만 정리함
  * https://medium.com/@TimvanBaarsen/help-my-spring-boot-info-actuator-endpoint-is-enabled-but-i-dont-see-any-environment-details-c2d41a7b24d7
* `application.properties`
```properties
# disk, db 정보 노출
management.endpoint.health.show-details=always
# health 를 제외하곤 모두 숨긴상태이기에 노출
management.endpoints.web.exposure.include=httptrace,health,info,loggers
# info 에 자바정보와 커스텀 정보 노출
management.info.java.enabled=true
management.info.env.enabled=true
```
* info 에 깃정보 노출(git.properties 생성) :: https://github.com/n0mer/gradle-git-properties
* info 에 빌드 정보 노출(build-info.properties 생성) :: `build.gradle` 
```groovy
springBoot{
    buildInfo()
}
```
* httptrace 노출
  * https://stackoverflow.com/questions/59115578/httptrace-endpoint-of-spring-boot-actuator-doesnt-exist-anymore-with-spring-b
  * `HttpTraceRepository` 빈생성 필요
#### 다양한 운영 데이터 확인
* heapdump
  * https://visualvm.github.io/download.html
  * 윈도우11 에서 실행시 자바를 찾지 못하는 경우 `etc/visualvm.conf` 에서 `visualvm_jdkhome` 항목 설정 필요
* httptrace 
  * 위에서 살펴본 내용이 여기서 나오는군...
  * `SpringDataHttpTraceRepository` 를 이용하는 부분이 해보고 싶으나 `HttpTraceWrapperRepository` 가 빈으로 등록되지 않는 에러 발생
  * 왜 등록이 안되는지 참 ㅜㅜ
```java
Description:

Parameter 0 of method springDataTraceRepository in mingu.bookreactive.config.HttpTraceActuatorConfiguration required a bean of type 'mingu.bookreactive.repository.HttpTraceWrapperRepository' that could not be found.


Action:

Consider defining a bean of type 'mingu.bookreactive.repository.HttpTraceWrapperRepository' in your configuration.
```
---
## 6장 스프링 부트 API 서버 구축
* asciidoctor 는 다음에 해보는걸로 ㅋ
---
## 7장 스프링 부트 메시징
* testcontainer 는 너무 좋음 (https://mvnrepository.com/artifact/org.testcontainers)
* rabbitmq 테스트를 위해 `build.gradle` 추가
```groovy
    // rabbitmq
    implementation 'org.springframework.boot:spring-boot-starter-amqp'

    // testcontainers
    testImplementation 'org.testcontainers:junit-jupiter:1.16.3'
    testImplementation 'org.testcontainers:rabbitmq:1.16.3'
```
* `@DynamicPropertySource` 를 이용한 config 설정 (이걸 몰랐네)
* rabbitmq 관련한 `SpringAmqpItemController`, `SpringAmqpItemService`, `JacksonConfig` 등록
* 로깅 레벨 설정
```properties
logging.level.org.springframework.amqp=DEBUG
logging.level.org.springframework.messaging=DEBUG
logging.level.org.springframework.data=DEBUG
logging.level.reactor=DEBUG
logging.level.mingu.bookreactive=DEBUG
```
* Application 실행은 에러가 발생하며 (Amqp 설정이 없음), 테스트만 실행 확인
* 만약 로컬에 rabbitmq가 필요하다면 도커로 실행 및 property 설정
  * 포트를 5672 로 하게 되면, 이번에는 테스트가 실패하게 되니 알아서 잘 해야 함
```yaml
version: '3'
services:
  rabbitmq:
    image: rabbitmq:3.7.25-management-alpine
    container_name: rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: "admin"
      RABBITMQ_DEFAULT_PASS: "admin"
```
```properties
# ch8 rabbitmq
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=admin
```
---
## 8장 부터는 모듈 분리 진행
* 경로 관련 항목을 모두 /ch1to7 추가 필요함