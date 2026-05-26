--liquibase formatted sql

--changeset client-service:v7_create_table_response
create table if not exists response
(
    id                           uuid PRIMARY KEY,
    external_response_id         uuid    not null,
    first_name                   varchar not null,
    last_name                    varchar not null,
    patronymic                   varchar,
    birth_date                   date    not null,
    gender                       varchar not null,
    identity_document_series     varchar not null,
    identity_document_number     varchar not null,
    identity_document_issue_date date    not null,
    itn                          varchar,
    insurance_number             varchar,
    residence_address_name       varchar not null,
    actual_date                  timestamp with time zone,

    CONSTRAINT fk_response_external_response
        FOREIGN KEY (external_response_id)
            REFERENCES external_response (id)
);

COMMENT ON TABLE response IS 'Данные клиента, полученные от внешнего источника';

COMMENT ON COLUMN response.id IS 'Идентификатор данных клиента из ответа';
COMMENT ON COLUMN response.external_response_id IS 'Идентификатор внешнего ответа';
COMMENT ON COLUMN response.first_name IS 'Имя';
COMMENT ON COLUMN response.last_name IS 'Фамилия';
COMMENT ON COLUMN response.patronymic IS 'Отчество';
COMMENT ON COLUMN response.birth_date IS 'Дата рождения';
COMMENT ON COLUMN response.gender IS 'Пол';
COMMENT ON COLUMN response.identity_document_series IS 'Серия документа';
COMMENT ON COLUMN response.identity_document_number IS 'Номер документа';
COMMENT ON COLUMN response.identity_document_issue_date IS 'Дата выдачи документа';
COMMENT ON COLUMN response.itn IS 'ИНН';
COMMENT ON COLUMN response.insurance_number IS 'СНИЛС';
COMMENT ON COLUMN response.residence_address_name IS 'Адрес места жительства';
COMMENT ON COLUMN response.actual_date IS 'Дата актуальности данных';
