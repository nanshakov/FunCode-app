# nanshakov App
Задание : 

## Принцип работы
Приложение состоит из грабберов, очереди сообщений и воркеров, которые разбирают эту очередь и сохраняют в базу и в s3.
Приложение реализовано на базе Spring и умеет в конфигурацию
Приложение поддерживает 28 языков из коробки, распознование построено на основе словарей.

### Конфигурирование грабберов
Пример конфигурации
```
#IFUNNY,NineGag,Reddit
type: "IFUNNY,NineGag,Reddit"
lang: German

IFUNNY:
  download-url: "https://img.ifunny.co/images/"
  tags: "deutsch,german"
  recursion:
    enable: true
    depth: 10000
    duplicates-count: 50 #сколько должно быть дубликатов подряд, что бы произошло переключение тега.

NineGag:
  download-url: "https://9gag.com/v1/search-posts?query="
  tags: "deutsch"
  recursion:
    enable: true
    depth: 10000
    duplicates-count: 50 #сколько должно быть дубликатов подряд, что бы произошло переключение тега.

Reddit:
  tags: "asozialesnetzwerk,ich_iel,kreiswichs,Lustig,NichtDerPostillon,schland,wasletztepreis,austria,switzerland,liechtenstein,Luxembourg,DSA_RPG,PietSmiet,zocken,600euro,aeiou,doener,DEjobs,depression_de,de_IAmA,de_EDV,de_it,erasmus,finanzen,fragreddit,germantrees,GermanyPics,Geschichte,kochen,recht,veganDE,Weibsvolk,wissenschaft"
  username: "nanshakov"
  password: "112233"
  clientId: "zCfokV2al_OClQ"
  clientSecret: "qLckFw3cI4-546QhO0yZLouWiDQ"
  recursion:
    enable: true
    depth: 10000
    duplicates-count: 50 #сколько должно быть дубликатов подряд, что бы произошло переключение тега.
```

`type` - перечесление воркеров по именам
`lang` - язык для проверки контента
```
Arabic, Armenian, Catalan, Croatian, Czech, Dutch(), 
    Danish(), English(), Esperanto(), Farsi(), Finnish(), 
    French(true), German(), Greek(), Hindi(), Hungarian(), 
    Italian(), Latin(), Norwegian(), Polish(), Portuguese(), 
    Romanian(), Russian(), Slovenian(), Slovak(), Spanish
    Swedish(), Hebrew(), Turkish()
`tags` - список тегов для парсинга
```
  recursion: 
    enable: true - включить рекурсивный обход тегов
    depth: 10000 - максимальная глубина рекурсии
    duplicates-count: 50 #сколько должно быть дубликатов подряд, что бы произошло переключение тега.
```

## Сборка
`
./mvnw clean package 
`
## Запуск

Dockerfile

## Необходимые сервисы
| Название |
| ------ |
| clickhouse |
| redis |
| kafka |
| s3 |
### Конфигурирование сервисов
clickhouse
```
spring:
  datasource:
    driver-class-name: "ru.yandex.clickhouse.ClickHouseDriver"
    url: "jdbc:clickhouse://docker01....:8123/default"
    username: ""
    password: ""
```
redis
```
spring:
  redis:
    host: docker01....
    port: 6379
```
kafka
```
spring:
    kafka:
        topic: "nanshakov20"
        producer:
          bootstrap:
            servers: docker01....:9092
        consumer:
          bootstrap:
            servers: docker01....:9092
          group:
            id: 1
          auto-offset-reset: earliest
```
s3
```
s3.endpoint: "http://docker01..."
s3.accessKey: "minioadmin"
s3.secretKey: "minioadmin"
s3.bucket: "nanshakov"
s3.port: 9999
```
## Эндпоинты

## Метрики

