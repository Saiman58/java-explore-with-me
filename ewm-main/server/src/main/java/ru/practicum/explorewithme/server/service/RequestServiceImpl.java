package ru.practicum.explorewithme.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.server.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.server.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.server.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.server.entity.*;
import ru.practicum.explorewithme.server.exception.ConflictException;
import ru.practicum.explorewithme.server.exception.NotFoundException;
import ru.practicum.explorewithme.server.mapper.RequestMapper;
import ru.practicum.explorewithme.server.repository.EventRepository;
import ru.practicum.explorewithme.server.repository.RequestRepository;
import ru.practicum.explorewithme.server.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение заявок пользователя с id: {}", userId);

        checkUserExists(userId);

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);

        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание заявки: пользователь {} на событие {}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        // 1. Нельзя добавить повторный запрос
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Заявка пользователя " + userId +
                    " на событие " + eventId + " уже существует");
        }

        // 2. Инициатор события не может добавить запрос на участие в своём событии
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подать заявку на участие в своём событии");
        }

        // 3. Нельзя участвовать в неопубликованном событии
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        // 4. Проверка лимита участников
        Long confirmedRequests = requestRepository.countConfirmedRequests(eventId);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников для события " + eventId);
        }

        // Создаем заявку
        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(user)
                .build();

        // Определяем статус
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        log.info("Заявка создана с id: {}", savedRequest.getId());

        return requestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена заявки {} пользователем {}", requestId, userId);

        checkUserExists(userId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка с id=" + requestId + " не найдена"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Заявка " + requestId + " не принадлежит пользователю " + userId);
        }

        request.setStatus(RequestStatus.CANCELED);

        ParticipationRequest updatedRequest = requestRepository.save(request);
        log.info("Заявка {} отменена", requestId);

        return requestMapper.toDto(updatedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        log.info("Получение участников события {} для пользователя {}", eventId, userId);

        checkUserExists(userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Пользователь " + userId +
                    " не является инициатором события " + eventId);
        }

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);

        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(
            Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {

        log.info("Обновление статуса заявок для события {} пользователем {}", eventId, userId);

        checkUserExists(userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Пользователь " + userId +
                    " не является инициатором события " + eventId);
        }

        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(updateRequest.getRequestIds());

        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new NotFoundException("Заявка " + request.getId() +
                        " не принадлежит событию " + eventId);
            }
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        Long confirmedCount = requestRepository.countConfirmedRequests(eventId);
        int participantLimit = event.getParticipantLimit();

        if (updateRequest.getStatus() == EventRequestStatusUpdateRequest.StatusAction.CONFIRMED) {
            for (ParticipationRequest request : requests) {
                if (!request.getStatus().equals(RequestStatus.PENDING)) {
                    throw new ConflictException("Заявка должна иметь статус PENDING");
                }

                if (participantLimit > 0 && confirmedCount >= participantLimit) {
                    throw new ConflictException("Достигнут лимит участников");
                }

                request.setStatus(RequestStatus.CONFIRMED);
                requestRepository.save(request);
                confirmed.add(requestMapper.toDto(request));
                confirmedCount++;

                if (participantLimit > 0 && confirmedCount >= participantLimit) {
                    rejectPendingRequests(eventId);
                    break;
                }
            }
        } else {
            for (ParticipationRequest request : requests) {
                if (!request.getStatus().equals(RequestStatus.PENDING)) {
                    throw new ConflictException("Заявка должна иметь статус PENDING");
                }
                request.setStatus(RequestStatus.REJECTED);
                requestRepository.save(request);
                rejected.add(requestMapper.toDto(request));
            }
        }

        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);

        return result;
    }

    // Вспомогательные методы

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private void rejectPendingRequests(Long eventId) {
        log.info("Автоматическое отклонение ожидающих заявок для события {}", eventId);

        List<ParticipationRequest> pendingRequests = requestRepository.findAllByEventId(eventId).stream()
                .filter(request -> request.getStatus().equals(RequestStatus.PENDING))
                .collect(Collectors.toList());

        for (ParticipationRequest request : pendingRequests) {
            request.setStatus(RequestStatus.REJECTED);
            requestRepository.save(request);
        }
    }
}