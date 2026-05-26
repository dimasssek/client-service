--liquibase formatted sql

--changeset client-service:v9_alter_table_client_drop_column_deleted
alter table client drop column if exists deleted;
