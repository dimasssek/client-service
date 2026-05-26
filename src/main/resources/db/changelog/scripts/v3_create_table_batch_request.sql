--liquibase formatted sql

--changeset client-service:v3_create_table_batch_request
create table if not exists batch_request
(
    id            uuid PRIMARY KEY,
    message_id    uuid,
    created_date  timestamp with time zone,
    request_id    uuid,
    message_count integer,

    CONSTRAINT fk_batch_request_request
        FOREIGN KEY (request_id)
            REFERENCES external_request (id)
);

COMMENT ON TABLE batch_request IS 'Пакетный запрос';

COMMENT ON COLUMN batch_request.id IS 'Идентификатор запроса';
COMMENT ON COLUMN batch_request.message_id IS 'Идентификатор сообщения';
COMMENT ON COLUMN batch_request.created_date IS 'Дата получения ответа';
COMMENT ON COLUMN batch_request.request_id IS 'Внешний ключ на external_request';
COMMENT ON COLUMN batch_request.message_count IS 'Количество человек в одном сообщении';
