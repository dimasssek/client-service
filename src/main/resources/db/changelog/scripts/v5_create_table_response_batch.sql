--liquibase formatted sql

--changeset client-service:v5_create_table_response_batch
create table if not exists response_batch
(
    id               uuid PRIMARY KEY,
    batch_request_id uuid,
    source_type      varchar                  not null,
    message_id       uuid,
    received_date    timestamp with time zone not null default now(),

    CONSTRAINT fk_response_batch_batch_request
        FOREIGN KEY (batch_request_id)
            REFERENCES batch_request (id)
);

COMMENT ON TABLE response_batch IS 'Пачка ответа от внешнего источника';

COMMENT ON COLUMN response_batch.id IS 'Идентификатор пачки ответа';
COMMENT ON COLUMN response_batch.batch_request_id IS 'Идентификатор запроса-пачки, на который получен ответ';
COMMENT ON COLUMN response_batch.source_type IS 'Тип внешнего источника, от которого получен ответ';
COMMENT ON COLUMN response_batch.message_id IS 'Идентификатор сообщения ответа';
COMMENT ON COLUMN response_batch.received_date IS 'Дата получения пачки ответа';
