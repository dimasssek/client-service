--liquibase formatted sql

--changeset client-service:v8_create_table_outbox_message
create table if not exists outbox_message
(
    id              uuid PRIMARY KEY,
    aggregate_type  varchar                  not null,
    aggregate_id    uuid                     not null,
    event_type      varchar                  not null,
    payload         jsonb                    not null,
    exchange_name   varchar                  not null,
    routing_key     varchar                  not null,
    status          varchar                  not null,
    attempts        integer                  not null default 0,
    last_error      text,
    created_at      timestamp with time zone not null default now(),
    sent_at         timestamp with time zone
);

create index if not exists ix_outbox_message_status_created on outbox_message (status, created_at);

COMMENT ON TABLE outbox_message IS 'Outbox-сообщения для надёжной публикации в RabbitMQ';

COMMENT ON COLUMN outbox_message.id IS 'Идентификатор outbox-записи';
COMMENT ON COLUMN outbox_message.aggregate_type IS 'Тип агрегата (CLIENT, REQUEST и т.д.)';
COMMENT ON COLUMN outbox_message.aggregate_id IS 'Идентификатор агрегата';
COMMENT ON COLUMN outbox_message.event_type IS 'Тип события';
COMMENT ON COLUMN outbox_message.payload IS 'Тело сообщения в формате JSON';
COMMENT ON COLUMN outbox_message.exchange_name IS 'Имя exchange для публикации';
COMMENT ON COLUMN outbox_message.routing_key IS 'Routing key для публикации';
COMMENT ON COLUMN outbox_message.status IS 'Статус обработки outbox-записи';
COMMENT ON COLUMN outbox_message.attempts IS 'Количество попыток отправки';
COMMENT ON COLUMN outbox_message.last_error IS 'Текст последней ошибки отправки';
COMMENT ON COLUMN outbox_message.created_at IS 'Дата создания outbox-записи';
COMMENT ON COLUMN outbox_message.sent_at IS 'Дата успешной отправки';
