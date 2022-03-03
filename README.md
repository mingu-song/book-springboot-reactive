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
