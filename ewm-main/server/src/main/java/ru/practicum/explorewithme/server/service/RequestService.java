package ru.practicum.explorewithme.server.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.server.entity.Event;
import ru.practicum.explorewithme.server.entity.EventState;
import ru.practicum.explorewithme.server.entity.Request;
import ru.practicum.explorewithme.server.entity.RequestStatus;
import ru.practicum.explorewithme.server.exception.ConflictException;
import ru.practicum.explorewithme.server.exception.EntityNotFoundException;
import ru.practicum.explorewithme.server.repository.EventRepository;
import ru.practicum.explorewithme.server.repository.RequestRepository;
import ru.practicum.explorewithme.server.mapper.RequestMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final RequestMapper requestMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String REQUEST_NOT_FOUND = "Request with id=%d was not found";
    private static final String NOT_YOUR_REQUEST = "This is not your request";
    private static final String NOT_YOUR_EVENT = "This is not your event";
    private static final String LIMIT_REACHED = "The participation limit has been reached";
    private static final String ALREADY_REQUESTED = "Already requested participation in this event";
    private static final String CANNOT_CANCEL_CONFIRMED = "Only pending or rejected requests can be canceled";
    private static final String CANNOT_MODIFY_NON_PENDING = "Request must have status PENDING";

    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        log.info("Попытка создания запроса на участие: пользователь ID={}, событие ID={}", userId, eventId);

        if (eventId == null) {
            log.warn("Конфликт: eventId равен null для пользователя ID={}", userId);
            throw new IllegalArgumentException("Параметр 'eventId' обязателен");
        }

        userService.getById(userId);

        Event event = eventRepository.findByIdWithInitiator(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId().equals(userId)) {
            log.warn("Конфликт: пользователь ID={} пытается участвовать в своем событии ID={}", userId, eventId);
            throw new ConflictException("Нельзя запрашивать участие в своем же собственном событии");
        }

        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Конфликт: событие ID={} не опубликовано (состояние={})", eventId, event.getState());
            throw new ConflictException("Событие не опубликовано");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            log.warn("Конфликт: уже существует запрос от пользователя ID={} на событие ID={}", userId, eventId);
            throw new ConflictException(ALREADY_REQUESTED);
        }

        Long confirmedCount = getConfirmedCount(eventId);
        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            log.warn("Конфликт: достигнут лимит участников для события ID={}: текущий счёт={}, лимит={}",
                    eventId, confirmedCount, event.getParticipantLimit());
            throw new ConflictException(LIMIT_REACHED);
        }

        RequestStatus status;
        if (event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = event.getRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
        }

        Request request = Request.builder()
                .event(event)
                .requester(userService.getById(userId))
                .status(status)
                .created(LocalDateTime.now())
                .build();

        request = requestRepository.save(request);
        entityManager.flush();

        log.info("Успешно создан запрос на участие: ID={}, статус={}", request.getId(), status.name());
        return requestMapper.toDto(request);
    }

    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        log.info("Попытка отмены запроса: пользователь ID={}, запрос ID={}", userId, requestId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(REQUEST_NOT_FOUND, requestId)));

        if (!request.getRequester().getId().equals(userId)) {
            log.warn("Ошибка прав: пользователь ID={} пытается отменить чужой запрос ID={}", userId, requestId);
            throw new IllegalArgumentException(NOT_YOUR_REQUEST);
        }

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            log.warn("Конфликт: попытка отмены подтверждённого запроса ID={}", requestId);
            throw new ConflictException(CANNOT_CANCEL_CONFIRMED);
        }

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        entityManager.flush();

        log.info("Успешно отменён запрос: ID={}", requestId);
        return requestMapper.toDto(request);
    }

    public List<ParticipationRequestDto> getByUser(Long userId) {
        log.info("Получение запросов пользователя ID={}", userId);

        userService.getById(userId);
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        log.info("Найдено {} запросов для пользователя ID={}", requests.size(), userId);

        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ParticipationRequestDto> getByEvent(Long userId, Long eventId) {
        log.info("Получение запросов для события: инициатор ID={}, событие ID={}", userId, eventId);

        userService.getById(userId);

        Event event = eventRepository.findByIdWithInitiator(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("Ошибка прав: пользователь ID={} пытается получить запросы чужого события ID={}", userId, eventId);
            throw new IllegalArgumentException(NOT_YOUR_EVENT);
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);
        log.info("Найдено {} запросов для события ID={}", requests.size(), eventId);

        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult changeStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest update
    ) {
        log.info("Попытка изменения статусов запросов: инициатор ID={}, событие ID={}, новый статус={}, запросов={}",
                userId, eventId, update.getStatus(), update.getRequestIds().size());

        userService.getById(userId);

        Event event = eventRepository.findByIdWithInitiator(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("Ошибка прав: пользователь ID={} пытается изменить статусы чужого события ID={}", userId, eventId);
            throw new IllegalArgumentException(NOT_YOUR_EVENT);
        }

        List<Request> requests = update.getRequestIds().stream()
                .map(id -> requestRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException(
                                String.format(REQUEST_NOT_FOUND, id))))
                .collect(Collectors.toList());

        boolean hasNotPending = requests.stream()
                .anyMatch(r -> r.getStatus() != RequestStatus.PENDING);

        if (hasNotPending) {
            log.warn("Конфликт: попытка изменить заявку не в статусе PENDING для события ID={}", eventId);
            throw new ConflictException(CANNOT_MODIFY_NON_PENDING);
        }

        Long confirmedCount = getConfirmedCount(eventId);

        if ("CONFIRMED".equals(update.getStatus())
                && event.getParticipantLimit() > 0
                && confirmedCount >= event.getParticipantLimit()) {
            log.warn("Конфликт: лимит участников уже достигнут для события ID={}", eventId);
            throw new ConflictException(LIMIT_REACHED);
        }

        List<Request> pendingRequests = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .collect(Collectors.toList());

        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        for (Request r : pendingRequests) {
            if ("CONFIRMED".equals(update.getStatus())) {
                r.setStatus(RequestStatus.CONFIRMED);
                confirmed.add(r);
            } else {
                r.setStatus(RequestStatus.REJECTED);
                rejected.add(r);
            }
        }

        requestRepository.saveAll(pendingRequests);
        entityManager.flush();

        List<ParticipationRequestDto> additionalRejected = new ArrayList<>();
        if ("CONFIRMED".equals(update.getStatus())
                && event.getParticipantLimit() > 0
                && confirmedCount + confirmed.size() >= event.getParticipantLimit()) {

            List<Request> remainingPending = requestRepository
                    .findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);

            remainingPending.forEach(r -> r.setStatus(RequestStatus.REJECTED));
            requestRepository.saveAll(remainingPending);
            entityManager.flush();

            additionalRejected = remainingPending.stream()
                    .map(requestMapper::toDto)
                    .collect(Collectors.toList());

            log.info("Автоматически отклонено {} дополнительных запросов для события ID={}",
                    additionalRejected.size(), eventId);
        }

        List<ParticipationRequestDto> confirmedDtos = confirmed.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());

        List<ParticipationRequestDto> rejectedDtos = rejected.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());

        rejectedDtos.addAll(additionalRejected);

        log.info("Успешно обновлены статусы: подтверждено={}, отклонено={} для события ID={}",
                confirmedDtos.size(), rejectedDtos.size(), eventId);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedDtos)
                .rejectedRequests(rejectedDtos)
                .build();
    }

    public Long getConfirmedCount(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }
}