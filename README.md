# client-service

Документация для агентов, gateway и UI: [docs/](docs/).

Микросервис работы с клиентами банка: CRUD клиентов, исходящие запросы во внешние ведомства (ФНС/ЕПГУ), приём ответов, outbox-публикация событий в RabbitMQ.

## Стек

- Java 21, Spring Boot 4.0.6, Maven
- PostgreSQL 16, RabbitMQ 3, Liquibase
- MapStruct, QueryDSL, Testcontainers
- Контракты: git submodule `src/contracts` → [dimasssek/contracts](https://github.com/dimasssek/contracts.git)

## Быстрый старт

### 1. Инфраструктура

> **Используйте [bsps-infra](https://github.com/dimasssek/bsps-infra)** — единый `docker compose` для всей инфраструктуры.
> Локальный `docker-compose.yml` в этом репозитории **deprecated**.

```bash
cd ../bsps-infra
docker compose up -d
```

| Сервис      | Порт  |
|-------------|-------|
| Postgres    | 5488  |
| RabbitMQ    | 5672  |
| RabbitMQ UI | 15672 |

Подробнее: `bsps-infra/docs/LOCAL_SETUP.md`. Параметры подключения — в `application.yml`.

### 2. Submodule контрактов

```bash
git submodule update --init --recursive
```

### 3. Запуск

```bash
./mvnw spring-boot:run
```

Сервис: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 4. Эмулятор ведомств (локально)

В `application.yml`:

```yaml
app:
  emulator:
    enabled: true
    min-delay-ms: 10000
    max-delay-ms: 20000
```

Эмулятор слушает очереди FNS/EPGU, через задержку публикует ответ в `client.external-response.exchange`. Ответы обрабатывает `ExternalResponseListener`.

## API

Префикс `/api/v1` не используется — его добавляет gateway.

### Клиенты

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/clients` | Создание |
| GET | `/clients/{id}` | Получение |
| PUT | `/clients/{id}` | Обновление |
| DELETE | `/clients/{id}` | Soft delete (`status=DELETED`) |
| POST | `/clients/search` | Поиск с пагинацией |
| GET | `/clients/{id}/history` | История запросов во внешние ведомства |

### Внешние запросы

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/external-requests/batch` | Запрос по списку `clientId` |
| POST | `/external-requests/manual` | Ручной ввод данных |
| GET | `/external-requests/{id}` | Агрегат + summary по outcome |
| GET | `/external-requests/{id}/batches` | Пакетные запросы |
| POST | `/external-requests/search` | Поиск |

### Пакетные запросы

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/batch-requests/{id}/requests` | Запросы по клиентам в пачке |

Данные `Response` от ведомств **наружу не отдаются** — только статусы и `outcome` (`PENDING`, `UPDATED`, `ACTUAL`, `NOT_FOUND`, `ERROR`).

## Outbox

Планировщик `OutboxPublisher` опрашивает `outbox_message` (`status=NEW`, `FOR UPDATE SKIP LOCKED`) и публикует в RabbitMQ.

```yaml
app:
  outbox:
    poll-interval-ms: 5000
    batch-size: 50
    max-attempts: 5
    max-error-length: 2000
```

## RabbitMQ topology

| Exchange | Назначение |
|----------|------------|
| `client.events.exchange` | События CRUD клиента |
| `client.external-request.exchange` | Исходящие запросы в ФНС/ЕПГУ |
| `client.external-response.exchange` | Входящие ответы и рассылки |

| Queue | Consumer |
|-------|----------|
| `client.external-request.fns.queue` | Эмулятор / внешняя система |
| `client.external-request.epgu.queue` | Эмулятор / внешняя система |
| `client.external-response.queue` | `ExternalResponseListener` |
| `client.external-broadcast.queue` | `BroadcastListener` |

## Тесты

```bash
./mvnw test
```

Интеграционные тесты используют Testcontainers (Postgres 16 + RabbitMQ). Планировщик outbox в тестах отключён (`spring.task.scheduling.enabled=false`); publisher вызывается явно.

## Структура проекта

```
src/main/java/ru/kubsu/clientservice/
├── controller/       REST API
├── service/          Бизнес-логика
├── outbox/           Outbox writer + publisher
├── listener/         RabbitMQ consumers
├── integration/      Эмулятор ведомств (+ Feign-заглушки, не подключены)
├── response/         Обработка ответов и рассылок
├── config/           RabbitMQ, outbox, emulator properties
└── entity/           JPA-сущности

src/contracts/        Git submodule — DTO, enums, messaging, exceptions
```

## Contracts submodule

Контракты живут в отдельном репозитории и подключаются как submodule:

```bash
# после изменений в src/contracts
cd src/contracts
git add .
git commit -m "..."
git push

cd ../..
git add src/contracts
git commit -m "Update contracts submodule"
```

Подробнее: [src/contracts/README.md](src/contracts/README.md)
