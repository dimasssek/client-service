--liquibase formatted sql

--changeset client-service:v10_alter_table_request_add_column_outcome
alter table request
    add column if not exists outcome varchar;

comment on column request.outcome is 'Итог обработки ответа ведомства';
