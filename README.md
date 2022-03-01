# 스프링 부트 실전 활용 마스터
* 원본 깃헙 :: https://github.com/onlybooks/spring-boot-reactive
* spring boot 2.6.4 / gradle build / java 11 로 변경하여 진행
---
### 2장 스프링 부트를 활용한 데이터 엑세스
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
spring.data.mongodb.host=localhost
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
### 3장 스프링 부트 개발자 도구
