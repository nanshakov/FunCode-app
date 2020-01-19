# nanshakov FunCode-app
## Принцип работы
Приложение состоит из грабберов, очереди сообщений и воркеров, которые разбирают эту очередь и сохраняют в базу и в s3.
Приложение реализовано на базе Spring и умеет в конфигурацию
Приложение поддерживает 28 языков из коробки, распознование построено на основе словарей.

Какие дополнительные фичи удалось реализовать:

| Фича |
| ------ |
| дополнительные языки: французский, испанский, итальянский, португальский, русский  |
| сортировку ленты от более интересного контента к менее интересному (на основе метаданных) |
| endpoint с Prometheus-метриками, чем подробнее — тем лучше |

## Архитектура
### Алгоритм работы парсера
1 Получить ссылку или обьект json
2 Разобрать на элементы
3 Проверить язык
4 Сходить в Redis, записать обьект в горячий кеш или дропнуть, если он там есть (вернутся к п.1)
5 Отправить в кафку
### Алгоритм работы воркера
1 Получить сообщение из кафки
2 Посчитать hash от url и проверить его в clickhouse (на случай перезапуска всей системы, либо инвалидации горячего кеша). Если обьект есть в clickhouse -> п.1
3 Скачать контент (изображение)
4 Сохранить в s3 
5 Сохранить метаданные в clickhouse
### Конфигурирование грабберов
Пример конфигурации
```
#IFUNNY,NineGag,Reddit
type: "IFUNNY,NineGag,Reddit"
lang: German
multi-instance: false - обязательно включить при 2х и более инстансах
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
  password: ""
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
Arabic, Armenian, Catalan, Croatian, Czech, Dutch, 
    Danish, English, Esperanto, Farsi, Finnish, 
    French(true), German, Greek, Hindi, Hungarian, 
    Italian, Latin, Norwegian, Polish, Portuguese, 
    Romanian, Russian, Slovenian, Slovak, Spanish
    Swedish, Hebrew, Turkish
```
`tags` - список тегов для парсинга, через запятую, либо "тредов". Поволяет конфигурировать "источники"
```
  recursion: 
    enable: true - включить рекурсивный обход тегов
    depth: 10000 - максимальная глубина рекурсии
    duplicates-count: 50 #сколько должно быть дубликатов подряд, что бы произошло переключение тега.
```

для Reddita использовалась библиотека https://mattbdean.gitbooks.io/jraw/, необходимо зарегестрировать приложение и создать токены (https://mattbdean.gitbooks.io/jraw/oauth2.html)

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
### Обязательно сконфигурировать
```
spring.datasource.url=
spring.redis.host=
spring.kafka.producer.bootstrap.servers=
spring.kafka.consumer.bootstrap.servers=
s3.endpoint=
s3.accessKey=
s3.secretKey=
s3.bucket=
```
## Эндпоинты
| Путь | Описание |
| ------ | ------ |
|GET /feed | Лента с сортировкой для самых новых |
|GET /feed/hot | Лента самых популярных |
|GET /feed/{id} | Отдельно пост по id |

параметры запросов 
| Параметр | Описание |
| ------ | ------ |
|pageNum | Номер страницы default 1|
|count | Количество default 50|

## Метрики

| Параметр | Описание |
| ------ | ------ |
|parse.duplicates | Количество дубликатов, которые найдены в процессе парсинга|
|parse.successful | Количество успешно (прошло проверку на дубликаты и на язык) постов|
|parse.error | Количество ошибок получения \ разбора контента|
|parse.drop | Количество отброшенных по языковому признаку постов|
|processing.successful | Количество успешно сохраненных в БД (прошло проверку на дубликаты) постов|
|processing.duplicates |  Количество дубликатов, которые найдены в процессе обработки|
|processing.error | Количество ошибок получения \ разбора контента|

Конкурсное задание

Domain terms (словарь предметной области)

Content (контент) — базовая единица просмотра, картинка или видео длиной до 5 минут.
Metadata (метаданные) — сопровождающая контент информация (дата/время публикации, источник, автор, язык, число пользовательских реакций, число комментариев и т.п.).
Feed (лента) — последовательность контентов, предназначенная для просмотра.

Задача

Напишите приложение на Java или/и KotlIn, которое с максимальной скоростью распознает и соберёт все мемы на немецком языке и вернёт их в качестве ленты контента, сопровождаемого метаданными, отсортированной по времени публикации от более свежих к более старым.

Термин «мемы» мы трактуем широко: это могут быть тематические картинки, комиксы, забавные видео, рисунки, производные от аниме и манги и т.п.

Контент в приложении должен быть уникальным, то есть нужно отфильтровать дубликаты. Алгоритм любой, от хэш-суммы файла до нечёткого сравнения.

Настройка источников мемов должна быть достаточно гибкой, чтобы добавление новых можно было обеспечить редактированием конфигурационных файлов.

Предполагается, что лента отдаётся в REST-парадигме: endpoint GET /feed, возвращающий постраничный список контента для предпросмотра, и endpoint GET /feed/:contentID, возвращающий отдельные контенты с полными метаданными.

Приложение будет работать в Docker-контейнере, а значит в корне репозитория должен лежать Dockerfile и приложение должно запускаться по команде: docker run --rm -it $(docker build -q .)

Приложение должно соответствовать основным принципам 12-factor:

    Писать логи в STDOUT без промежуточной буферизации
    Конфигурироваться через environment-переменные
    Не хранить состояние
    Масштабироваться горизонтально 


Под последним пунктом мы понимаем конфигурацию, когда приложение развёрнуто в кластере из нескольких хостов и запросы между ними распределяет балансировщик. Все хосты кластера получают одинаковый набор environment-переменных. Все нужные вашему приложению внешние сервисы (базы данных, кэши, объектные или файловые хранилища) также должны задаваться конфигурационными параметрами. Если что, то наш любимый стек — Redis, MongoDB и AWS S3.

Конкурсное задание должно сопровождаться файлом README с описанием сборки и запуска, пример конфигурации переменных среды можно положить рядом в файле .env.example

God-mode (AKA задачи со звёздочкой)

Если задание показалось вам слишком лёгким, вы можете добавить в него:

    дополнительные языки: французский, испанский, итальянский, португальский, русский;
    фронтенд для просмотра ленты;
    сортировку ленты от более интересного контента к менее интересному (на основе метаданных);
    противодействие rate-limiting на стороне серверов с мемами;
    другие классные фишки на ваш выбор 

Мы будем оценивать:

    архитектурное решение;
    качество кода;
    производительность;
    параллельную неблокирующую работу отдельных компонентов приложения;
    потребление ресурсов (CPU, память, внешние хранилища) 

Следующие критерии также положительно повлияют на оценку жюри:

    наличие в git-репо актуальной истории коммитов;
    healthcheck и readiness check;
    endpoint с Prometheus-метриками, чем подробнее — тем лучше;
    юнит-тесты;
    функциональные тесты Docker-образа;
    OpenAPI-документация;
    подробное руководство по эксплуатации (runbook).

В процессе оценки мы запустим каждое прошедшее предварительный отбор приложение на один час в трёх экземплярах. Соберём метрики загрузки CPU, памяти и сети, а также оценим количество и качество уникального полученного контента.

Желаем удачи!
