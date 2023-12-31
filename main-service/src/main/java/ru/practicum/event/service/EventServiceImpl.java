package ru.practicum.event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.ClientStatistic;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventStatatus;
import ru.practicum.event.model.dto.*;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.mapper.LocationMapper;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.model.dto.ParticipationRequestDto;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventServiceImpl implements EventService {


    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final ClientStatistic statClient;
    private final CategoryMapper categoryMapper;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserMapper userMapper;
    private final LocationMapper locationMapper;

    public EventServiceImpl(EventMapper eventMapper, EventRepository eventRepository,
                            CategoryRepository categoryRepository, NamedParameterJdbcTemplate namedJdbcTemplate,
                            ClientStatistic statClient, CategoryMapper categoryMapper,
                            UserRepository userRepository, RequestRepository requestRepository,
                            RequestMapper requestMapper, UserMapper userMapper, LocationMapper locationMapper) {
        this.eventMapper = eventMapper;
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.namedJdbcTemplate = namedJdbcTemplate;
        this.statClient = statClient;
        this.categoryMapper = categoryMapper;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.userMapper = userMapper;
        this.locationMapper = locationMapper;
    }

    @Override
    public EventDto find(long id) {
        Event event = findEvent(id);
        if (event.getState() != EventStatatus.PUBLISHED) {
            log.info("Событие {} не опубликовано", event);
            throw new NotFoundException();
        }
        EventDto eventDto = eventMapper.toEventFullDto(event);
        eventDto.setViews(getNumberViews(id));
        eventDto.setConfirmedRequests(getNumberConfirmedRequests(event));
        log.info("Найдено событие {}", eventDto);
        return eventDto;
    }

    @Override
    public List<EventMinDto> getAll(String text, List<Long> categories, Boolean paid,
                                    LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                    Boolean onlyAvailable, String sort, Integer from,
                                    Integer size) {

        validateParameters(from, size);
        if (categories != null) {
            for (Long categoryId : categories) {
                if (categoryId == 0) {
                    log.info("В запросе указан неверный идентификатор события {}", categoryId);
                    throw new ValidationException();
                }
            }
        }
        String sql = "SELECT *\n" +
                "FROM PUBLIC.EVENTS AS EVENTS\n" +
                "LEFT JOIN\n" +
                "\t(SELECT REQ.EVENT_ID AS EVENT_ID,\n" +
                "\t\t\tCOUNT(REQ.ID) AS AMOUNT\n" +
                "\t\tFROM PUBLIC.REQUESTS AS REQ\n" +
                "\t\tWHERE REQ.STATUS = 'CONFIRMED'\n" +
                "\t\tGROUP BY REQ.EVENT_ID) AS REQUESTS ON (EVENTS.ID = REQUESTS.EVENT_ID\n" +
                "AND EVENTS.PARTICIPANT_LIMIT >= REQUESTS.AMOUNT)\n" +
                "WHERE STATE = 'PUBLISHED'";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        if (text != null && !text.isBlank()) {
            sql += " AND ((UPPER(events.annotation) LIKE UPPER(CONCAT('%', :text, '%')))" +
                    "OR (UPPER(events.description) LIKE UPPER(CONCAT('%', :text, '%'))))";
            parameters.addValue("text", text);
        }
        if (categories != null && !categories.isEmpty()) {
            sql += " AND (events.category_id IN (:categories))";
            parameters.addValue("categories", categories);
        }
        if (paid != null) {
            sql += " AND (events.paid = :paid)";
            parameters.addValue("paid", paid);
        }
        if (rangeStart != null && rangeEnd != null) {
            sql += " AND (events.event_date >= :start AND events.event_date <= :end)";
            parameters.addValue("start", rangeStart);
            parameters.addValue("end", rangeEnd);
        } else {
            sql += " AND (events.event_date >= :now)";
            parameters.addValue("now", LocalDateTime.now());
        }
        if (onlyAvailable != null && onlyAvailable) {
            sql += " AND (requests.amount IS NOT NULL)";
        }
        sql += " LIMIT " + size + " OFFSET " + from;
        List<EventMinDto> events = namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> createEventShort(rs));
        if (sort != null) {
            switch (sort) {
                case "EVENT_DATE":
                    events = events.stream()
                            .sorted(Comparator.comparing(EventMinDto::getEventDate))
                            .collect(Collectors.toList());
                    break;
                case "VIEWS":
                    events = events.stream()
                            .sorted(Comparator.comparing(EventMinDto::getViews))
                            .collect(Collectors.toList());
                    break;
            }
        }
        log.info("Найдены события {}", events);
        return events;
    }

    @Override
    public List<EventDto> getAdminAllEvents(List<Long> users,
                                            List<String> states,
                                            List<Long> categories,
                                            LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd,
                                            int from,
                                            int size) {
        validateParameters(from, size);
        List<EventStatatus> eventStatatuses = new ArrayList<>();
        if (states != null) {
            for (String state : states) {
                EventStatatus eventStatatus = EventStatatus.valueOf(state);
                eventStatatuses.add(eventStatatus);
            }
        }
        List<EventDto> eventsFullDto = new ArrayList<>();
        List<Event> events;
        if (users == null) {
            events = eventRepository.findAll(PageRequest.of(from, size)).getContent();
        } else {
            events = eventRepository.findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateIsAfterAndEventDateIsBefore(
                    users,
                    eventStatatuses,
                    categories,
                    rangeStart,
                    rangeEnd,
                    PageRequest.of(from, size));
        }
        log.info("Получены события {}", events);
        for (Event event : events) {
            EventDto eventDto = eventMapper.toEventFullDto(event);
            eventDto.setViews(getNumberViews(event.getId()));
            eventDto.setConfirmedRequests(getNumberConfirmedRequests(event));
            eventsFullDto.add(eventDto);
        }
        log.info("Найдены представления событий {}", eventsFullDto);
        return eventsFullDto;
    }

    @Override
    public EventDto updateAdminEvent(long eventId, ChangeEventAdminRequest updateRequest) {
        Event event = findEvent(eventId);
        if (updateRequest == null) {
            log.info("Не указаны поля для обновления события. Тело запроса пустое");
            return eventMapper.toEventFullDto(event);
        }
        String stateAction = updateRequest.getStateAction();
        if (stateAction != null) {
            if (!event.getState().equals(EventStatatus.PENDING)) {
                log.info("Событие можно публиковать или отклонить, только если оно в состоянии ожидания публикации." +
                        "Текущий статус события - {}", event.getState());
                throw new ConflictException();
            }
            switch (stateAction) {
                case "PUBLISH_EVENT":
                    event.setState(EventStatatus.PUBLISHED);
                    event.setPublishedDate(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    event.setState(EventStatatus.CANCELED);
                    break;
            }
        }
        String eventDate = updateRequest.getEventDate();
        if (eventDate != null) {
            LocalDateTime newEventDate = LocalDateTime.parse(eventDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (newEventDate.isBefore(LocalDateTime.now())) {
                log.info("Новая дата {} события уже наступила", newEventDate);
                throw new ValidationException();
            }
            if (event.getState().equals(EventStatatus.PUBLISHED)) {
                LocalDateTime publishedDate = event.getPublishedDate();
                long diff = ChronoUnit.HOURS.between(publishedDate, newEventDate);
                if (diff >= 0 && diff <= 1) {
                    log.info("Дата начала {} изменяемого события должна быть не ранее, чем за час от даты публикации {}",
                            newEventDate,
                            publishedDate);
                    throw new ConflictException();
                }
            }
            event.setEventDate(newEventDate);
        }
        if (updateRequest.getTitle() != null) {
            if (updateRequest.getTitle().length() < 3) {
                log.info("В запросе на обновление события от пользователя длина заголовка {} меньше 3 символов",
                        updateRequest.getTitle());
                throw new ValidationException();
            }
            if (updateRequest.getTitle().length() > 120) {
                log.info("В запросе на обновление события от пользователя длина заголовка {} больше 120 символов",
                        updateRequest.getTitle());
                throw new ValidationException();
            }
            event.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(updateRequest.getLocation()));
        }
        if (updateRequest.getDescription() != null) {
            if (updateRequest.getDescription().length() < 20) {
                log.info("В запросе на обновление события от пользователя длина описания {} меньше 20 символов",
                        updateRequest.getDescription());
                throw new ValidationException();
            }
            if (updateRequest.getDescription().length() > 7000) {
                log.info("В запросе на обновление события от пользователя длина описания {} больше 7000 символов",
                        updateRequest.getDescription());
                throw new ValidationException();
            }
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getCategory() != null) {
            event.setCategory(findCategory(updateRequest.getCategory()));
        }
        if (updateRequest.getAnnotation() != null) {
            if (updateRequest.getAnnotation().length() < 20) {
                log.info("В запросе на обновление события от пользователя длина аннотации {} меньше 20 символов",
                        updateRequest.getAnnotation());
                throw new ValidationException();
            }
            if (updateRequest.getAnnotation().length() > 2000) {
                log.info("В запросе на обновление события от пользователя длина аннотации {} больше 2000 символов",
                        updateRequest.getAnnotation());
                throw new ValidationException();
            }
            event.setAnnotation(updateRequest.getAnnotation());
        }
        Event updatedEvent = eventRepository.save(event);
        log.info("Обновлено событие {} на основании запроса администратора {}", updatedEvent, updateRequest);
        return eventMapper.toEventFullDto(updatedEvent);
    }


    @Override
    public List<EventMinDto> getUserEvents(long userId, int from, int size) {
        User initiator = findUser(userId);
        List<Event> userEvents = eventRepository.findAllByInitiator(initiator, PageRequest.of(from, size));
        log.info("Найдены события {} пользователя {}", userEvents, initiator);
        return userEvents.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventDto createUserEvent(long userId, FreshEventDto freshEventDto) {
        validateNewEventDto(freshEventDto);
        User initiator = findUser(userId);
        Event newEvent = eventMapper.toEvent(freshEventDto);
        newEvent.setInitiator(initiator);
        newEvent.setState(EventStatatus.PENDING);
        newEvent.setCreationDate(LocalDateTime.now());
        if (newEvent.getRequestModeration() == null) {
            newEvent.setRequestModeration(true);
        }
        if (newEvent.getPaid() == null) {
            newEvent.setPaid(false);
        }
        if (newEvent.getParticipantLimit() == null) {
            newEvent.setParticipantLimit(0);
        }
        LocalDateTime eventDate = newEvent.getEventDate();
        if (eventDate != null) {
            if (eventDate.isBefore(LocalDateTime.now())) {
                log.info("Дата и время {} события указаны в прошлом времени", eventDate);
                throw new ValidationException();
            }
            long diff = ChronoUnit.HOURS.between(LocalDateTime.now(), eventDate);
            if (diff >= 0 && diff <= 2) {
                log.info("Дата и время {}, на которые намечено событие {}, не может быть раньше, чем через 2 часа от текущего момента",
                        eventDate,
                        newEvent);
                throw new ConflictException();
            }
        }
        Event savedEvent = eventRepository.save(newEvent);
        log.info("Добавлено новое событие {} от пользователя {}", savedEvent, initiator);
        return eventMapper.toEventFullDto(newEvent);
    }


    @Override
    public EventDto findUserEvent(long userId, long eventId) {
        User initiator = findUser(userId);
        Event foundEvent = findEvent(eventId, initiator);
        log.info("Найдено событие {} от пользователя {}", foundEvent, initiator);
        return eventMapper.toEventFullDto(foundEvent);
    }


    @Override
    public EventDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest updateRequest) {
        User initiator = findUser(userId);
        Event event = findEvent(eventId, initiator);
        if (updateRequest == null) {
            log.info("Не указаны поля для обновления события. Тело запроса пустое");
            return eventMapper.toEventFullDto(event);
        }
        if (updateRequest.getAnnotation() != null) {
            if (updateRequest.getAnnotation().length() < 20) {
                log.info("В запросе на обновление события от пользователя длина аннотации {} меньше 20 символов",
                        updateRequest.getAnnotation());
                throw new ValidationException();
            }
            if (updateRequest.getAnnotation().length() > 2000) {
                log.info("В запросе на обновление события от пользователя длина аннотации {} больше 2000 символов",
                        updateRequest.getAnnotation());
                throw new ValidationException();
            }
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            event.setCategory(findCategory(updateRequest.getCategory()));
        }
        if (updateRequest.getDescription() != null) {
            if (updateRequest.getDescription().length() < 20) {
                log.info("В запросе на обновление события от пользователя длина описания {} меньше 20 символов",
                        updateRequest.getDescription());
                throw new ValidationException();
            }
            if (updateRequest.getDescription().length() > 7000) {
                log.info("В запросе на обновление события от пользователя длина описания {} больше 7000 символов",
                        updateRequest.getDescription());
                throw new ValidationException();
            }
            event.setDescription(updateRequest.getDescription());
        }
        String eventDate = updateRequest.getEventDate();
        if (eventDate != null) {
            LocalDateTime newEventDate = LocalDateTime.parse(eventDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (newEventDate.isBefore(LocalDateTime.now())) {
                log.info("Новая дата {} события уже наступила", newEventDate);
                throw new ValidationException();
            }
            if (event.getState().equals(EventStatatus.PUBLISHED)) {
                long diff = ChronoUnit.HOURS.between(newEventDate, LocalDateTime.now());
                if (diff <= 2) {
                    log.info("дата и время {}, на которые намечено событие, не может быть раньше, чем через два часа от текущего момента {}",
                            newEventDate,
                            LocalDateTime.now());
                    throw new ConflictException();
                }
            }
            event.setEventDate(newEventDate);
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(updateRequest.getLocation()));
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(event.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            if (updateRequest.getTitle().length() < 3) {
                log.info("В запросе на обновление события от пользователя длина заголовка {} меньше 3 символов",
                        updateRequest.getTitle());
                throw new ValidationException();
            }
            if (updateRequest.getTitle().length() > 120) {
                log.info("В запросе на обновление события от пользователя длина заголовка {} больше 120 символов",
                        updateRequest.getTitle());
                throw new ValidationException();
            }
            event.setTitle(updateRequest.getTitle());
        }
        String stateAction = updateRequest.getStateAction();
        if (stateAction != null) {
            if (event.getState().equals(EventStatatus.PUBLISHED)) {
                log.info("Изменить можно только отмененные события или события в состоянии ожидания модерации." +
                                "Текущий статус - {}",
                        event.getState());
                throw new ConflictException();
            }
            switch (stateAction) {
                case "SEND_TO_REVIEW":
                    event.setState(EventStatatus.PENDING);
                    break;
                case "CANCEL_REVIEW":
                    event.setState(EventStatatus.CANCELED);
                    break;
            }
        }
        Event updatedEvent = eventRepository.save(event);
        log.info("Обновлено событие {} на основании запроса пользователя {}", updatedEvent, updateRequest);
        return eventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    public List<ParticipationRequestDto> findUserEventRequests(long userId, long eventId) {
        User initiator = findUser(userId);
        Event event = findEvent(eventId, initiator);
        List<Request> requests = requestRepository.findAllByEvent(event);
        log.info("Найдены запросы {} на участие в событии {} пользователя {}", requests, event, initiator);
        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateUserEventRequests(long userId, long eventId, EventRequestStatusUpdateRequest updateRequest) {
        User initiator = findUser(userId);
        Event event = findEvent(eventId, initiator);
        RequestStatus updateStatus = updateRequest.getStatus();
        if (Boolean.FALSE.equals(event.getRequestModeration()) && updateStatus.equals(RequestStatus.CONFIRMED)) {
            return EventRequestStatusUpdateResult.builder().build();
        }
        if (getNumberConfirmedRequests(event) == event.getParticipantLimit() && updateStatus.equals(RequestStatus.CONFIRMED)) {
            log.info("Нельзя подтвердить заявки, если уже достигнут лимит по заявкам на событие {}", event);
            throw new ConflictException();
        }
        List<Long> requestsIdForUpdate = updateRequest.getRequestIds();
        List<Request> requests = requestRepository.findAllByIdInAndEvent(requestsIdForUpdate, event);
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        boolean isLimitReached = getNumberConfirmedRequests(event) == event.getParticipantLimit();
        for (Request request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                log.info("Статус можно изменить только у заявки, находящейся в состоянии ожидания." +
                        "Текущий статус - {}", request.getStatus());
                throw new ConflictException();
            }
            if (isLimitReached) {
                updateStatus = RequestStatus.REJECTED;
            }
            request.setStatus(updateStatus);
            Request savedRequest = requestRepository.save(request);
            log.info("Изменен запрос на участие {}", savedRequest);
            ParticipationRequestDto participationRequestDto = requestMapper.toParticipationRequestDto(request);
            if (updateStatus.equals(RequestStatus.CONFIRMED)) {
                confirmedRequests.add(participationRequestDto);
                log.info("Заявка {} на участие в событии {} подтверждена", request, event);
            } else if (updateStatus.equals(RequestStatus.REJECTED)) {
                rejectedRequests.add(participationRequestDto);
                log.info("Заявка {} на участие в событии {} отменена", request, event);
            }
            if (!isLimitReached && (getNumberConfirmedRequests(event) == event.getParticipantLimit())) {
                isLimitReached = true;
                log.info("Достигнут лимит заявок по участию в событии {}", event);
            }
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    private Event findEvent(long eventId, User initiator) {
        if (eventId == 0) {
            log.info("Идентификатор события для поиска равен 0");
            throw new ValidationException();
        }
        Optional<Event> event = eventRepository.findByIdAndInitiator(eventId, initiator);
        if (event.isEmpty()) {
            log.info("Не найдено событие с идентификатором {} у инициатора {} ", eventId, initiator);
            throw new NotFoundException();
        }
        return event.get();
    }

    private void validateNewEventDto(FreshEventDto freshEventDto) {
        if (freshEventDto == null) {
            log.info("Не указаны поля для создания нового события. Тело запроса пустое");
            throw new ValidationException();
        }
        if (freshEventDto.getDescription() == null) {
            log.info("Не указано поле 'description' для создания нового события");
            throw new ValidationException();
        }
        if (freshEventDto.getDescription().isBlank()) {
            log.info("Поле 'description' для создания нового события - пустое");
            throw new ValidationException();
        }
        if (freshEventDto.getDescription().length() < 20) {
            log.info("Поле 'description' для создания нового события меньше 20 символов");
            throw new ValidationException();
        }
        if (freshEventDto.getAnnotation() == null) {
            log.info("Не указано поле 'annotation' для создания нового события");
            throw new ValidationException();
        }
        if (freshEventDto.getAnnotation().isBlank()) {
            log.info("Поле 'annotation' для создания нового события - пустое");
            throw new ValidationException();
        }
        if (freshEventDto.getAnnotation().length() < 20) {
            log.info("Поле 'annotation' для создания нового события меньше 20 символов");
            throw new ValidationException();
        }
        if (freshEventDto.getAnnotation().length() > 2000) {
            log.info("Поле 'annotation' для создания нового события больше 2000 символов");
            throw new ValidationException();
        }
        if (freshEventDto.getTitle() == null) {
            log.info("Не указано поле 'title' для создания нового события");
            throw new ValidationException();
        }
        if (freshEventDto.getTitle().length() < 3) {
            log.info("Поле 'title' для создания нового события меньше 3 символов");
            throw new ValidationException();
        }
        if (freshEventDto.getTitle().length() > 120) {
            log.info("Поле 'title' для создания нового события больше 120 символов");
            throw new ValidationException();
        }
    }

    private User findUser(long userId) {
        if (userId == 0) {
            log.info("Идентификатор пользователя для поиска равен 0");
            throw new ValidationException();
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.info("Не найден пользователь с идентификатором {}", userId);
            throw new NotFoundException();
        }
        return user.get();
    }

    private Event findEvent(long eventId) {
        if (eventId == 0) {
            log.info("Идентификатор события для поиска равен 0");
            throw new ValidationException();
        }
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            log.info("Не найдено событие с идентификатором {}", eventId);
            throw new NotFoundException();
        }
        return event.get();
    }


    private int getNumberViews(long eventId) {
        String[] uris = {"/events/" + eventId};
        String startDate = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endDate = LocalDateTime.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        ResponseEntity<Object> stats = statClient.getStats(startDate, endDate, uris, true);
        int views = 0;
        if (stats.hasBody()) {
            List<HashMap<String, Object>> body = (List<HashMap<String, Object>>) stats.getBody();
            if (body != null && !body.isEmpty()) {
                HashMap<String, Object> map = body.get(0);
                views = (int) map.get("hits");
            }
        }
        return views;
    }

    private int getNumberConfirmedRequests(Event event) {
        return (int) event.getRequests().stream()
                .filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED))
                .count();
    }


    private Category findCategory(long catId) {
        if (catId == 0) {
            log.info("Идентификатор категории для поиска равен 0");
            throw new ValidationException();
        }
        Optional<Category> category = categoryRepository.findById(catId);
        if (category.isEmpty()) {
            log.info("Не найдена категория с идентификатором {}", catId);
            throw new NotFoundException();
        }
        return category.get();
    }

    private void validateParameters(int from, int size) {
        if (from < 0) {
            log.info("Параметр 'from' должен быть больше или равен 0, указано значение {}", from);
            throw new ValidationException();
        } else if (size <= 0) {
            log.info("Параметр 'size' должен быть больше 0, указано значение {}", size);
            throw new ValidationException();
        }
    }

    private EventMinDto createEventShort(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        LocalDateTime eventDate = LocalDateTime.parse(rs.getString("event_date"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return EventMinDto.builder()
                .id(id)
                .annotation(rs.getString("annotation"))
                .category(categoryMapper.toCategoryDto(findCategory(rs.getLong("category_id"))))
                .confirmedRequests(rs.getInt("amount"))
                .eventDate(eventDate)
                .initiator(userMapper.toUserShortDto(findUser(rs.getLong("initiator_id"))))
                .paid(rs.getBoolean("paid"))
                .title(rs.getString("title"))
                .views(getNumberViews(id))
                .build();
    }


}