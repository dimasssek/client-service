package ru.kubsu.clientservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.kubsu.clientservice.entity.OutboxMessage;
import ru.kubsu.clientservice.repository.OutboxMessageRepository;
import ru.kubsu.clientservice.service.ClientPersistenceService;
import ru.kubsu.clientservice.support.ClientTestFixtures;
import ru.kubsu.clientservice.support.EntityTestFixtures;
import ru.kubsu.clientservice.support.IntegrationTestBase;
import ru.kubsu.contracts.dto.service.client.ClientCreateRequest;
import ru.kubsu.contracts.dto.service.client.ClientQueryParams;
import ru.kubsu.contracts.dto.service.client.ClientTo;
import ru.kubsu.contracts.dto.service.client.ClientUpdateRequest;
import ru.kubsu.contracts.enums.service.client.ClientStatus;
import ru.kubsu.contracts.enums.service.client.OutboxEventType;
import ru.kubsu.contracts.exception.service.client.ClientNotFoundException;
import ru.kubsu.contracts.exception.service.client.ClientValidationException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты CRUD API клиентов.
 */
class ClientCrudIT extends IntegrationTestBase {

    /** Сервис персистентности клиентов для подготовки данных. */
    @Autowired
    private ClientPersistenceService clientPersistenceService;

    /** Репозиторий outbox-сообщений. */
    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    /**
     * Проверяет создание клиента и запись outbox-события CLIENT_CREATED.
     */
    @Test
    void should_createClient_whenValidPayload() throws Exception {
        ClientCreateRequest request = ClientTestFixtures.validCreateRequest();

        String responseJson = mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lastName").value("Иванов"))
                .andExpect(jsonPath("$.status").value("ACTUAL"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ClientTo created = jsonMapper.readValue(responseJson, ClientTo.class);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getActualDate()).isNotNull();

        assertOutboxEvent(created.getId(), OutboxEventType.CLIENT_CREATED);
    }

    /**
     * Проверяет ответ 404 при запросе несуществующего клиента.
     */
    @Test
    void should_returnNotFound_whenClientMissing() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(get("/clients/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ClientNotFoundException.ERROR_CODE));
    }

    /**
     * Проверяет поиск клиентов по частичному совпадению фамилии.
     */
    @Test
    void should_searchClients_byPartialLastName() throws Exception {
        clientPersistenceService.save(EntityTestFixtures.validClient().setLastName("Сидоров"));
        clientPersistenceService.save(EntityTestFixtures.validClient().setLastName("Сидоренко"));

        ClientQueryParams params = new ClientQueryParams()
                .setLastName("сидор");

        mockMvc.perform(post("/clients/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(params)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    /**
     * Проверяет поиск клиентов по частичному совпадению адреса.
     */
    @Test
    void should_searchClients_byPartialAddress() throws Exception {
        clientPersistenceService.save(EntityTestFixtures.validClient()
                .setResidenceAddressName("г. Казань, ул. Баумана, д. 5"));
        clientPersistenceService.save(EntityTestFixtures.validClient()
                .setResidenceAddressName("г. Москва, ул. Тверская, д. 1"));

        ClientQueryParams params = new ClientQueryParams()
                .setResidenceAddressName("казань");

        mockMvc.perform(post("/clients/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(params)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].residenceAddressName").value("г. Казань, ул. Баумана, д. 5"));
    }

    /**
     * Проверяет, что по умолчанию в поиск попадают только ACTUAL-клиенты.
     */
    @Test
    void should_searchClients_withDefaultActualStatusOnly() throws Exception {
        clientPersistenceService.save(EntityTestFixtures.validClient().setLastName("Актуальный"));
        clientPersistenceService.save(EntityTestFixtures.validClient()
                .setLastName("Удалённый")
                .setStatus(ClientStatus.DELETED));

        mockMvc.perform(post("/clients/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new ClientQueryParams())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].lastName").value("Актуальный"));
    }

    /**
     * Проверяет поиск с явным указанием ACTUAL и DELETED в фильтре статусов.
     */
    @Test
    void should_searchClients_includingDeleted_whenStatusesContainDeleted() throws Exception {
        clientPersistenceService.save(EntityTestFixtures.validClient().setLastName("Актуальный"));
        clientPersistenceService.save(EntityTestFixtures.validClient()
                .setLastName("Удалённый")
                .setStatus(ClientStatus.DELETED));

        ClientQueryParams params = new ClientQueryParams()
                .setStatuses(Set.of(ClientStatus.ACTUAL, ClientStatus.DELETED));

        mockMvc.perform(post("/clients/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(params)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    /**
     * Проверяет ответ 400 при некорректном ключе сортировки.
     */
    @Test
    void should_returnBadRequest_whenSortKeyInvalid() throws Exception {
        ClientQueryParams params = new ClientQueryParams()
                .setSortKey("unknownField,asc");

        mockMvc.perform(post("/clients/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(params)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ClientValidationException.ERROR_CODE));
    }

    /**
     * Проверяет обновление клиента и запись outbox-события CLIENT_UPDATED.
     */
    @Test
    void should_updateClient_whenValidPayload() throws Exception {
        ClientTo created = createClientViaApi(ClientTestFixtures.validCreateRequest());
        ClientUpdateRequest updateRequest = ClientTestFixtures.validUpdateRequest();

        String responseJson = mockMvc.perform(put("/clients/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Петров"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ClientTo updated = jsonMapper.readValue(responseJson, ClientTo.class);
        assertThat(updated.getActualDate()).isAfterOrEqualTo(created.getActualDate());

        assertOutboxEvent(created.getId(), OutboxEventType.CLIENT_UPDATED);
    }

    /**
     * Проверяет soft delete клиента и запись outbox-события CLIENT_DELETED.
     */
    @Test
    void should_softDeleteClient() throws Exception {
        ClientTo created = createClientViaApi(ClientTestFixtures.validCreateRequest());

        mockMvc.perform(delete("/clients/{id}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clients/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELETED"));

        assertOutboxEvent(created.getId(), OutboxEventType.CLIENT_DELETED);
    }

    /**
     * Создаёт клиента через REST API.
     *
     * @param request запрос создания
     * @return созданный клиент
     */
    private ClientTo createClientViaApi(ClientCreateRequest request) throws Exception {
        String responseJson = mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return jsonMapper.readValue(responseJson, ClientTo.class);
    }

    /**
     * Проверяет наличие outbox-события для клиента.
     *
     * @param clientId  идентификатор клиента
     * @param eventType ожидаемый тип события
     */
    private void assertOutboxEvent(UUID clientId, OutboxEventType eventType) {
        List<OutboxMessage> messages = outboxMessageRepository.findAll().stream()
                .filter(message -> clientId.equals(message.getAggregateId()))
                .filter(message -> eventType == message.getEventType())
                .toList();

        assertThat(messages).hasSize(1);
        assertThat(messages.getFirst().getRoutingKey()).isNotBlank();
        assertThat(messages.getFirst().getPayload()).contains(clientId.toString());
    }
}
