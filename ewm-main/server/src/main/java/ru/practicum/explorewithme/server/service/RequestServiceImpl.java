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
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final RequestMapper requestMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String REQUEST_NOT_FOUND = "Заявка с id=%d не найдена";
    private static final String NOT_YOUR_REQUEST = "Это не ваша заявка";
    private static final String NOT_YOUR_EVENT = "Это не ваше событие";
    private static final String LIMIT_REACHED = "Достигнут лимит участников";
    private static final String ALREADY_REQUESTED = "Уже есть заявка на участие в этом событии";
    private static final String CANNOT_CANCEL_CONFIRMED = "Нельзя отменить подтверждённую заявку";
    private static final String CANNOT_MODIFY_NON_PENDING = "Заявка должна иметь статус PENDING";

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        log.info("[RequestService] Создание заявки: userId={}, eventId={}", userId, eventId);

        if (eventId == null) {
            log.warn("[RequestService] eventId равен null: userId={}", userId);
            throw new IllegalArgumentException("Параметр 'eventId' обязателен");
        }

        userService.getById(userId);

        Event event = eventRepository.findByIdWithInitiator(eventId)
                .orElseThrow(() -> {
                    log.error("[RequestService] Событие не найдено: id={}", eventId);
                    return new EntityNotFoundException("Событие с id=" + eventId + " не найдено");
                });

        if (event.getInitiator().getId().equals(userId)) {
            log.warn("[RequestService] Пользователь пытается участвовать в своём событии: userId={}, eventId={}",
                    userId, eventId);
            throw new ConflictException("Нельзя запрашивать участие в своём собственном событии");
        }

        if (event.getState() != EventState.PUBLISHED) {
            log.warn("[RequestService] Событие не опубликовано: eventId={}, state={}", eventId, event.getState());
            throw new ConflictException("Событие не опубликовано");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            log.warn("[RequestService] Заявка уже существует: userId={}, eventId={}", userId, eventId);
            throw new ConflictException(ALREADY_REQUESTED);
        }

        Long confirmedCount = getConfirmedCount(eventId);
        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            log.warn("[RequestService] Достигнут лимит участников: eventId={}, confirmed={}, limit={}",
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

        log.info("[RequestService] Заявка создана: id={}, status={}", request.getId(), status.name());
        return requestMapper.toDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        log.info("[RequestService] Отмена заявки: userId={}, requestId={}", userId, requestId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("[RequestService] Заявка не найдена: id={}", requestId);
                    return new EntityNotFoundException(String.format(REQUEST_NOT_FOUND, requestId));
                });

        if (!request.getRequester().getId().equals(userId)) {
            log.warn("[RequestService] Доступ запрещён: userId={}, requesterId={}", userId,
                    request.getRequester().getId());
            throw new IllegalArgumentException(NOT_YOUR_REQUEST);
        }

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            log.warn("[RequestService] Нельзя отменить подтверждённую заявку: id={}", requestId);
            throw new ConflictException(CANNOT_CANCEL_CONFIRMED);
        }

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        entityManager.flush();

        log.info("[RequestService] Заявка отменена: id={}", requestId);
        return requestMapper.toDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getByUser(Long userId) {
        log.debug("[RequestService] Получение заявок пользователя: userId={}", userId);

        userService.getById(userId);
        List<Request> requests = requestRepository.findAllByRequesterId(userId);

        log.debug("[RequestService] Найдено {} заявок: userId={}", requests.size(), userId);
        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getByEvent(Long userId, Long eventId) {
        log.debug("[RequestService] Получение заявок для события: userId={}, eventId={}", userId, eventId);

        userService.getById(userId);

        Event event = eventRepository.findByIdWithInitiator(eventId)
                .orElseThrow(() -> {
                    log.error("[RequestService] Событие не найдено: id={}", eventId);
                    return new EntityNotFoundException("Событие с id=" + eventId + " не найдено");
                });

        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("[RequestService] Доступ запрещён: userId={}, initiatorId={}", userId,
                    event.getInitiator().getId());
            throw new IllegalArgumentException(NOT_YOUR_EVENT);
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);
        log.debug("[RequestService] Найдено {} заявок для события: eventId={}", requests.size(), eventId);

        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest update
    ) {
        log.info("[RequestService] Изменение статусов: userId={}, eventId={}, status={}, count={}",
                userId, eventId, update.getStatus(), update.getRequestIds().size());

        userService.getById(userId);

        Event event = eventRepository.findByIdWithInitiator(eventId)
                .orElseThrow(() -> {
                    log.error("[RequestService] Событие не найдено: id={}", eventId);
                    return new EntityNotFoundException("Событие с id=" + eventId + " не найдено");
                });

        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("[RequestService] Доступ запрещён: userId={}, initiatorId={}", userId,
                    event.getInitiator().getId());
            throw new IllegalArgumentException(NOT_YOUR_EVENT);
        }

        List<Request> requests = update.getRequestIds().stream()
                .map(id -> requestRepository.findById(id)
                        .orElseThrow(() -> {
                            log.error("[RequestService] Заявка не найдена: id={}", id);
                            return new EntityNotFoundException(String.format(REQUEST_NOT_FOUND, id));
                        }))
                .collect(Collectors.toList());

        boolean hasNotPending = requests.stream()
                .anyMatch(r -> r.getStatus() != RequestStatus.PENDING);

        if (hasNotPending) {
            log.warn("[RequestService] Нельзя изменить заявку не в статусе PENDING: eventId={}", eventId);
            throw new ConflictException(CANNOT_MODIFY_NON_PENDING);
        }

        Long confirmedCount = getConfirmedCount(eventId);

        if ("CONFIRMED".equals(update.getStatus())
                && event.getParticipantLimit() > 0
                && confirmedCount >= event.getParticipantLimit()) {
            log.warn("[RequestService] Лимит участников достигнут: eventId={}, confirmed={}, limit={}",
                    eventId, confirmedCount, event.getParticipantLimit());
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
                log.debug("[RequestService] Заявка подтверждена: id={}", r.getId());
            } else {
                r.setStatus(RequestStatus.REJECTED);
                rejected.add(r);
                log.debug("[RequestService] Заявка отклонена: id={}", r.getId());
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

            log.info("[RequestService] Автоматически отклонено {} заявок: eventId={}",
                    additionalRejected.size(), eventId);
        }

        List<ParticipationRequestDto> confirmedDtos = confirmed.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());

        List<ParticipationRequestDto> rejectedDtos = rejected.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());

        rejectedDtos.addAll(additionalRejected);

        log.info("[RequestService] Статусы обновлены: подтверждено={}, отклонено={}, eventId={}",
                confirmedDtos.size(), rejectedDtos.size(), eventId);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedDtos)
                .rejectedRequests(rejectedDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getConfirmedCount(Long eventId) {
        Long count = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        log.debug("[RequestService] Подтверждённых заявок: eventId={}, count={}", eventId, count);
        return count != null ? count : 0L;
    }
}