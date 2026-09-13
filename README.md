# YandexBlog — бэкенд

Классический сайт-монолит для магазина на Spring Boot 3. Данные хранятся в H2, изображения товаров — в локальной директории.

## Требования

- **Java 21**
- Maven не нужен отдельно — в репозитории есть Maven Wrapper (`./mvnw`)

## Сборка

Скомпилировать проект и собрать артефакты:

```bash
./mvnw clean package
```

JAR появится в `./target/market-0.0.1-SNAPSHOT.jar`.

Или воспользоваться docker-compose, заменив переменные в .env:

```bash
docker compose up
```

## Тесты

Запустить все тесты:

```bash
./mvnw test
```

Тесты используют H2-базу в памяти и временную директорию для загрузок (см. `src/test/resources/application.properties`).

## Запуск


### Профиль `dev` (локальная разработка)

h2-база: `${java.io.tmpdir}/test`  
Директория для изображений: `${java.io.tmpdir}/yandex-blog-uploads/`

Или из JAR:

```bash
java -jar build/libs/yandex-blog-1.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### Профиль `prod`

Параметры подключения к БД задаются переменными окружения:

| Переменная | Описание |
|---|---|
| `DB_URL` | JDBC URL |
| `DB_USER` | Имя пользователя |
| `DB_PASS` | Пароль |

```bash
export DB_URL=jdbc:sqlite:/path/to/database.sqlite
export DB_USER=
export DB_PASS=

java -jar build/libs/yandex-blog-1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

По умолчанию сервер слушает порт **8080**.