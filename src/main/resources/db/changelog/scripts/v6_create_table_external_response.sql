--liquibase formatted sql

--changeset client-service:v6_create_table_external_response
create table if not exists external_response
(
    id                uuid PRIMARY KEY,
    response_batch_id uuid    not null,
    request_id        uuid    not null,
    correlation_id    uuid    not null,
    client_id         uuid,
    status            varchar not null,
    error_message     text,

    CONSTRAINT fk_external_response_response_batch
        FOREIGN KEY (response_batch_id)
            REFERENCES response_batch (id),

    CONSTRAINT fk_external_response_request
        FOREIGN KEY (request_id)
            REFERENCES request (id),

    CONSTRAINT fk_external_response_client
        FOREIGN KEY (client_id)
            REFERENCES client (id)
);

COMMENT ON TABLE external_response IS 'Ответ по конкретному клиентскому запросу';

COMMENT ON COLUMN external_response.id IS 'Идентификатор ответа';
COMMENT ON COLUMN external_response.response_batch_id IS 'Идентификатор пачки ответа';
COMMENT ON COLUMN external_response.request_id IS 'Идентификатор исходного запроса';
COMMENT ON COLUMN external_response.correlation_id IS 'Идентификатор корреляции для сопоставления ответа с исходным запросом';
COMMENT ON COLUMN external_response.client_id IS 'Идентификатор клиента в нашей системе';
COMMENT ON COLUMN external_response.status IS 'Статус ответа';
COMMENT ON COLUMN external_response.error_message IS 'Детализация ошибки';
