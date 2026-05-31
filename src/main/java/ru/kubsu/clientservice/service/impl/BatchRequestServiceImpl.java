package ru.kubsu.clientservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.mapper.RequestMapper;
import ru.kubsu.clientservice.repository.BatchRequestRepository;
import ru.kubsu.clientservice.repository.RequestRepository;
import ru.kubsu.clientservice.service.BatchRequestService;
import ru.kubsu.contracts.dto.service.client.RequestTo;
import ru.kubsu.contracts.exception.service.client.BatchRequestNotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса чтения пакетных запросов.
 */
@Service
@RequiredArgsConstructor
public class BatchRequestServiceImpl implements BatchRequestService {

    /** Репозиторий пакетных запросов. */
    private final BatchRequestRepository batchRequestRepository;

    /** Репозиторий запросов по клиентам. */
    private final RequestRepository requestRepository;

    /** MapStruct-маппер запросов. */
    private final RequestMapper requestMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<RequestTo> getRequests(UUID batchRequestId) {
        if (!batchRequestRepository.existsById(batchRequestId)) {
            throw new BatchRequestNotFoundException(batchRequestId);
        }
        return requestRepository.findByBatchRequest_IdOrderByIdAsc(batchRequestId).stream()
                .map(requestMapper::toRequestTo)
                .toList();
    }
}
