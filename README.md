# Описание принципов работы с S3 хранилищем Timeweb Cloud с примерами для разных языков

Все данные для подключения к S3 можно найти в [панели управления](https://vds.timeweb.ru/storage).

Для работы в конфигурационном файле используйте логин и пароль созданного пользователя:

- в качестве `Access Key` используется название аккаунта;
- в качестве `Secret Key` используется пароль пользователя;
- в качестве `Region` используется `ru-1`;
- `URL` для доступа (`endpoint_url`) [https://s3.timeweb.com](https://s3.timeweb.com).

На текущий момент реализована поддержка `AWS Signature V4`.

Для подтверждения личности запрашивающего все запросы должны иметь подпись,
которую можно создать с помощью `Access Key` и `Secret Key`.

## Вычисление подписи

Вычисление подписи состоит из трех шагов:

1. Получение ключа подписи (`SigningKey`)
2. Получение строки для подписи (`StringToSign`)
3. Подпись строки с помощью ключа

### Получение ключа для подписи (SigningKey)

Для получения подписывающего ключа закодируйте с помощью алгоритма `HMAC-SHA256` следующие данные:

```
DateKey = HMAC-SHA256("AWS4"+"<SecretKey>", "<YYYYMMDD>")
DateRegionKey = HMAC-SHA256(<DateKey>, "<aws-region>")
DateRegionServiceKey = HMAC-SHA256(<DateRegionKey>, "<aws-service>")
SigningKey = HMAC-SHA256(<DateRegionServiceKey>, "aws4_request")
```

Мы поддерживаем метод подписи запросов через `HTTP`-заголовок `Authorization`.

Использование заголовка `Authorization` является наиболее частным методом аутентификации пользователя.

Общий вид запроса:

```
Authorization: AWS4-HMAC-SHA256
Credential=12345_USER/20180524/ru-1/s3/aws4_request,
SignedHeaders=host;range;x-amz-date,
Signature=fe5f80f77d5fa3beca038a248ff027d0445342fe2855ddc963176630326f1024
```

Описание параметров запроса

| Параметр       | Описание                |
| ------------- | :------------------ |
| AWS4-HMAC-SHA256     | Подпись AWS версии 4 (AWS4) и алгоритм подписи (HMAC-SHA256)    |
| Credential     | Содержит ключ доступа и информацию о запросе в формате: ${ACCESS_KEY}/${YYYYMMDD}/${REGION}/s3/aws4_request |
| SignedHeaders  | Список в нижнем регистре имен заголовков запроса, используемых при вычислении подписи         |
| Signature  | Подписанный хэш, состоящий из хеша тела запроса, секретного ключа и информации о запросе (каноническом запросе)         |


### Получение строки для подписи

Для того чтобы получить строку для подписи, необходимо сделать канонический запрос следующего вида:

```
<HTTPMethod>\n
<CanonicalURI>\n
<CanonicalQueryString>\n
<CanonicalHeaders>\n
<SignedHeaders>\n
<HashedPayload>
```

Где:

**HTTPMethod** — один из `HTTP` методов, например `GET`, `PUT`, `HEAD` и `DELETE`;  
**CanonicalURI** — `URI`-кодированная часть адреса, которая начинается после домена, с первым «/», например для https://s3.timeweb.com/bucket/sample.txt будет выглядеть следующим образом: /bucket/sample.txt;  
**CanonicalQueryString** — параметры строки запроса;  
**CanonicalHeaders** — список заголовков и их значений, разделенных новой строкой, в нижнем регистре и без пробелов;  
**SignedHeaders** — список имен заголовков без значений, отсортированных по алфавиту, в нижнем регистре и через точку с запятой. Например: `host;x-amz-content-sha256;x-amz-date`;  
**HashedPayload** — хэш `SHA256` тела запроса `Hex(SHA256Hash())`. В случае, если тела запроса нет, хэш необходимо посчитать от пустой строки `Hex(SHA256Hash(“”))`.  

### Подпись строки с помощью ключа

Строка для подписи представляет собой конкатенацию следующих строк:

```
"AWS4-HMAC-SHA256" + "\n" +
timeStampISO8601Format + "\n" +
<Scope> + "\n" +
Hex(SHA256Hash(<CanonicalRequest>))
```

Где:

**AWS4-HMAC-SHA256** — данная строка определяет алгоритм шифрования, который вы используете;  
**timeStampISO8601Format** — текущее `UTC`-время в `ISO 8601` формате (например, `20130524T000000Z`);  
**Scope** — строка формата `date.Format(<YYYYMMDD>) + "/" + <region> + "/" + <service> + "/aws4_request"`, например `“20130606/ru-1/s3/aws4_request”`, привязывает полученную подпись к определенной дате, региону или сервису. В случае привязки к дате, подпись будет действовать 15 минут.  

Подробнее о способе аутентификации через `Authorization` заголовок читайте в официальной документации [Amazon S3 API](https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-auth-using-authorization-header.html).

## Поддержка методов S3

Облачное хранилище `Timeweb Cloud` обеспечивает совместимость с методами AWS S3: `Bucket CRUD`, `Bucket Location`, `Object CRUD`, `Object Copy`.

### Поддерживаемые методы для работы с бакетами

| Метод       | Описание                |
| ------------- | :------------------ |
| GET Service     | Получение списка бакетов    |
| GET Bucket (List Objects)     | Получение списка объектов в бакете    |
| GET Bucket Location     | Получение региона бакета    |
| DELETE Bucket     | Удаление бакета    |
| HEAD Bucket     | Получение статуса бакета    |
| PUT Bucket     | Создание бакета    |

### Поддерживаемые методы для работы с объектами

| Метод       | Описание                |
| ------------- | :------------------ |
| GET Object     | Получение объекта    |
| HEAD Object     | Получение метаданных объекта    |
| PUT Object     | Создание объекта    |
| PUT Object — Copy     | Копирование объекта    |
| DELETE Object     | Удаление объекта    |

## Примеры использования AWS SDK для разных языков

- [Node.js](https://github.com/timeweb/s3-examples/tree/master/nodejs)
- [Python3](https://github.com/timeweb/s3-examples/tree/master/python3)
- PHP

### Примечание

При использовании AWS SDK для соответствующего языка программирования необходимо чтобы формируемый урл имел вид `https://s3.timeweb.com/<имя бакета>/<имя файла>`. Например, в `PHP` при использовании `aws/aws-sdk-php` убедитесь, что в опциях стоит флаг `use_path_style_endpoint`.
