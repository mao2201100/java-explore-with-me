package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventMinDto;
import ru.practicum.event.dto.FreshEventDto;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "creationDate", source = "createdOn")
    @Mapping(target = "publishedDate", source = "publishedOn")
    Event toEvent(EventDto eventDto);

    @Mapping(target = "createdOn", source = "creationDate")
    @Mapping(target = "publishedOn", source = "publishedDate")
    EventDto toEventFullDto(Event event);

    Event toEvent(EventMinDto eventMinDto);

    EventMinDto toEventShortDto(Event event);

    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    Event toEvent(FreshEventDto freshEventDto);

    FreshEventDto toNewEventDto(Event event);

    default Category mapIdToCategory(Long catId) {
        return Category.builder()
                .id(catId)
                .build();
    }

    default Long mapCategoryToId(Category category) {
        return category.getId();
    }

    default LocalDateTime mapToLocalDateTime(LocalDateTime localDateTime) { //
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(localDateTime.format(formatter), formatter);
    }

}