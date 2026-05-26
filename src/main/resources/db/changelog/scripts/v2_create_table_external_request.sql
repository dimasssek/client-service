--liquibase formatted sql

--changeset client-service:v2_create_table_external_request
create table if not exists external_request
(
    id              uuid PRIMARY KEY,
    letter_number   varchar                  not null,
    letter_date     date                     not null,
    source_type     varchar                  not null,
    status          varchar                  not null,
    initiator_login varchar                  not null,
    created         timestamp with time zone not null default now()
);

COMMENT ON TABLE external_request IS 'Внешний запрос';

COMMENT ON COLUMN external_request.id IS 'Уникальный идентификатор внешнего запроса';
COMMENT ON COLUMN external_request.letter_number IS 'Номер исходящего письма';
COMMENT ON COLUMN external_request.letter_date IS 'Дата исходящего запроса';
COMMENT ON COLUMN external_request.source_type IS 'Тип внешнего источника-получателя';
COMMENT ON COLUMN external_request.status IS 'Статус';
COMMENT ON COLUMN external_request.initiator_login IS 'Ссылка на пользователя, который инициировал процесс создания запроса';
COMMENT ON COLUMN external_request.created IS 'Дата создания запроса';
