--liquibase formatted sql

--changeset client-service:v4_create_table_request
create table if not exists request
(
    id                           uuid PRIMARY KEY,
    client_id                    uuid,
    message_id                   uuid,
    batch_request_id             uuid,
    status                       varchar(255),
    error_message                text,
    first_name                   varchar(255),
    last_name                    varchar(255),
    patronymic                   varchar(255),
    birth_date                   date,
    gender                       varchar(50),
    identity_document_series     varchar(20),
    identity_document_number     varchar(50),
    identity_document_issue_date date,
    itn                          varchar(12),
    insurance_number             varchar(20),
    type                         varchar(50),

    CONSTRAINT fk_request_client
        FOREIGN KEY (client_id)
            REFERENCES client (id),

    CONSTRAINT fk_request_batch_request
        FOREIGN KEY (batch_request_id)
            REFERENCES batch_request (id)
);

COMMENT ON TABLE request IS 'Запрос';

COMMENT ON COLUMN request.id IS 'Идентификатор';
COMMENT ON COLUMN request.client_id IS 'Идентификатор клиента';
COMMENT ON COLUMN request.message_id IS 'Идентификатор сообщения, он же correlationId при взаимодействии';
COMMENT ON COLUMN request.batch_request_id IS 'Идентификатор запроса-пачки, частью которого является эта сущность';
COMMENT ON COLUMN request.status IS 'Статус запроса';
COMMENT ON COLUMN request.error_message IS 'Детализация ошибки';
COMMENT ON COLUMN request.first_name IS 'Имя';
COMMENT ON COLUMN request.last_name IS 'Фамилия';
COMMENT ON COLUMN request.patronymic IS 'Отчество';
COMMENT ON COLUMN request.birth_date IS 'Дата рождения';
COMMENT ON COLUMN request.gender IS 'Пол';
COMMENT ON COLUMN request.identity_document_series IS 'Серия документа';
COMMENT ON COLUMN request.identity_document_number IS 'Номер документа';
COMMENT ON COLUMN request.identity_document_issue_date IS 'Дата выдачи документа';
COMMENT ON COLUMN request.itn IS 'ИНН';
COMMENT ON COLUMN request.insurance_number IS 'СНИЛС';
COMMENT ON COLUMN request.type IS 'Тип запроса';
