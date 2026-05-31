package ru.kubsu.clientservice.service.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.mapper.ClientHistoryMapper;
import ru.kubsu.clientservice.mapper.ClientMapper;
import ru.kubsu.clientservice.outbox.OutboxWriter;
import ru.kubsu.clientservice.query.ClientPageableFactory;
import ru.kubsu.clientservice.query.ClientPredicateBuilder;
import ru.kubsu.clientservice.query.ClientSortParser;
import ru.kubsu.clientservice.query.PageDataBuilder;
import ru.kubsu.clientservice.repository.ClientRepository;
import ru.kubsu.clientservice.repository.RequestRepository;
import ru.kubsu.clientservice.service.ClientService;
import ru.kubsu.contracts.dto.service.client.ClientCreateRequest;
import ru.kubsu.contracts.dto.service.client.ClientHistoryEntryTo;
import ru.kubsu.contracts.dto.service.client.ClientQueryParams;
import ru.kubsu.contracts.dto.service.client.ClientTo;
import ru.kubsu.contracts.dto.service.client.ClientUpdateRequest;
import ru.kubsu.contracts.dto.service.client.PageData;
import ru.kubsu.contracts.enums.service.client.ClientStatus;
import ru.kubsu.contracts.enums.service.client.OutboxEventType;
import ru.kubsu.contracts.exception.service.client.ClientNotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса бизнес-логики работы с клиентами.
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    /** Репозиторий клиентов. */
    private final ClientRepository clientRepository;

    /** Репозиторий запросов по клиентам. */
    private final RequestRepository requestRepository;

    /** MapStruct-маппер клиента. */
    private final ClientMapper clientMapper;

    /** MapStruct-маппер истории клиента. */
    private final ClientHistoryMapper clientHistoryMapper;

    /** Запись outbox-сообщений. */
    private final OutboxWriter outboxWriter;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ClientTo create(ClientCreateRequest request) {
        Client client = clientMapper.toEntity(request)
                .setStatus(ClientStatus.ACTUAL)
                .setActualDate(OffsetDateTime.now());

        Client savedClient = clientRepository.save(client);
        outboxWriter.writeClientEvent(savedClient, OutboxEventType.CLIENT_CREATED);
        return clientMapper.toClientTo(savedClient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ClientTo getById(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        return clientMapper.toClientTo(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClientHistoryEntryTo> getHistory(UUID id) {
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException(id);
        }
        return requestRepository.findHistoryByClientId(id).stream()
                .map(request -> clientHistoryMapper.toHistoryEntry(
                        request, request.getBatchRequest().getExternalRequest()))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageData<ClientTo> search(ClientQueryParams params) {
        Predicate predicate = ClientPredicateBuilder.from(params);
        OrderSpecifier<?> orderSpecifier = ClientSortParser.parse(params.getSortKey());
        Pageable pageable = ClientPageableFactory.from(params, orderSpecifier);

        Page<Client> page = clientRepository.findAll(predicate, pageable);
        return PageDataBuilder.from(page, clientMapper::toClientTo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ClientTo update(UUID id, ClientUpdateRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        clientMapper.updateEntity(request, client);
        client.setActualDate(OffsetDateTime.now());

        Client savedClient = clientRepository.save(client);
        outboxWriter.writeClientEvent(savedClient, OutboxEventType.CLIENT_UPDATED);
        return clientMapper.toClientTo(savedClient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        client.setStatus(ClientStatus.DELETED);
        client.setActualDate(OffsetDateTime.now());

        Client savedClient = clientRepository.save(client);
        outboxWriter.writeClientEvent(savedClient, OutboxEventType.CLIENT_DELETED);
    }
}
