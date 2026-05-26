--liquibase formatted sql

--changeset client-service:v1_create_table_client
create table if not exists client
(
    id                           uuid PRIMARY KEY,
    last_name                    varchar not null,
    first_name                   varchar not null,
    patronymic                   varchar,
    birth_date                   date    not null,
    gender                       varchar not null,
    identity_document_series     varchar not null,
    identity_document_number     varchar,
    identity_document_issue_date date,
    itn                          varchar(12),
    insurance_number             varchar(20),
    actual_date                  timestamp with time zone,
    is_address_defined           boolean not null,
    residence_address_name       varchar not null,
    status                       varchar,
    deleted                      boolean not null
);

COMMENT ON TABLE client IS 'Клиент';

COMMENT ON COLUMN client.id IS 'Уникальный идентификатор клиента';
COMMENT ON COLUMN client.last_name IS 'Фамилия';
COMMENT ON COLUMN client.first_name IS 'Имя';
COMMENT ON COLUMN client.patronymic IS 'Отчество';
COMMENT ON COLUMN client.birth_date IS 'Дата рождения';
COMMENT ON COLUMN client.gender IS 'Пол';
COMMENT ON COLUMN client.identity_document_series IS 'Серия паспорта';
COMMENT ON COLUMN client.identity_document_number IS 'Номер паспорта';
COMMENT ON COLUMN client.identity_document_issue_date IS 'Дата выдачи паспорта';
COMMENT ON COLUMN client.itn IS 'ИНН';
COMMENT ON COLUMN client.insurance_number IS 'СНИЛС';
COMMENT ON COLUMN client.actual_date IS 'Последняя дата обновления записи';
COMMENT ON COLUMN client.residence_address_name IS 'Адрес места жительства';
COMMENT ON COLUMN client.is_address_defined IS 'Признак определенности адреса';
COMMENT ON COLUMN client.status IS 'Статус';
COMMENT ON COLUMN client.deleted IS 'Признак удаления записи';
