package ru.practicum.hit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ValidationException;
import ru.practicum.hit.dto.HitDTO;
import ru.practicum.hit.mapper.HitMapper;
import ru.practicum.hit.model.Hit;
import ru.practicum.statisticsDto.ViewStatsDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;
    private final HitMapper hitMapper;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public HitDTO createNewHit(HitDTO endpointHitDTO) {
        Hit endpointHit = hitMapper.toEndpointHit(endpointHitDTO);
        Hit savedEndpointHit = hitRepository.save(endpointHit);
        log.info("Информация сохранена {}", endpointHit);
        return hitMapper.toEndpointHitDto(savedEndpointHit);
    }

    @Override
    public List<ViewStatsDTO> readAllStats(LocalDateTime start, LocalDateTime end, List<String> uris, String unique) {
        if (start == null) {
            log.info("Не верная дата-время начала диапазона");
            throw new ValidationException();
        }
        if (end == null) {
            log.info("Не верная дата-время окончания диапазона");
            throw new ValidationException();
        }
        validateSearchDate(start, end);
        boolean onlyUnique = Boolean.parseBoolean(unique);
        List<ViewStatsDTO> viewStats;
        if (onlyUnique) {
            if (uris != null) {
                viewStats = getUniqueStatsFromStartToEndWithUris(start, end, uris);
            } else {
                viewStats = getUniqueStatsFromStartToEnd(start, end);
            }
        } else {
            if (uris != null) {
                viewStats = getStatsFromStartToEndWithUris(start, end, uris);
            } else {
                viewStats = getStatsFromStartToEnd(start, end);
            }
        }
        log.info("Статистика собрана {}", viewStats);
        return viewStats;
    }

    private void validateSearchDate(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate.isBefore(startDate)) {
            log.info("Дата окончания поиска {} не может быть раньше, чем даты начала {}", endDate, startDate);
            throw new ValidationException();
        }
    }

    private List<ViewStatsDTO> getUniqueStatsFromStartToEndWithUris(LocalDateTime startDate, LocalDateTime endDate, List<String> uris) {
        log.info("Extracting unique stats from start={} to end={} and uri in ({})", startDate, endDate, uris);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("start", startDate);
        parameters.addValue("end", endDate);
        parameters.addValue("uris", uris);
        String sql = "SELECT app, uri, COUNT(DISTINCT ip) AS hits\n" +
                "FROM public.hits\n" +
                "WHERE created_date >= :start AND created_date <= :end AND uri IN (:uris)\n" +
                "GROUP BY hits.app, hits.uri\n" +
                "ORDER BY COUNT(DISTINCT ip) DESC";
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> createViewStats(rs));
    }

    private List<ViewStatsDTO> getUniqueStatsFromStartToEnd(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Extracting unique stats from start={} to end={}", startDate, endDate);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("start", startDate);
        parameters.addValue("end", endDate);
        String sql = "SELECT app, uri, COUNT(DISTINCT ip) AS hits\n" +
                "FROM public.hits\n" +
                "WHERE created_date >= :start AND created_date <= :end\n" +
                "GROUP BY hits.app, hits.uri\n" +
                "ORDER BY COUNT(DISTINCT ip) DESC";
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> createViewStats(rs));
    }

    private List<ViewStatsDTO> getStatsFromStartToEndWithUris(LocalDateTime startDate, LocalDateTime endDate, List<String> uris) {
        log.info("Extracting all stats from start={} to end={} and uri in ({})", startDate, endDate, uris);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("start", startDate);
        parameters.addValue("end", endDate);
        parameters.addValue("uris", uris);
        String sql = "SELECT app, uri, COUNT(ip) AS hits\n" +
                "FROM public.hits\n" +
                "WHERE created_date >= :start AND created_date <= :end AND uri IN (:uris)\n" +
                "GROUP BY hits.app, hits.uri\n" +
                "ORDER BY COUNT(ip) DESC";
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> createViewStats(rs));
    }

    private List<ViewStatsDTO> getStatsFromStartToEnd(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Extracting all stats from start={} to end={}", startDate, endDate);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("start", startDate);
        parameters.addValue("end", endDate);
        String sql = "SELECT app, uri, COUNT(ip) AS hits\n" +
                "FROM public.hits\n" +
                "WHERE created_date >= :start AND created_date <= :end\n" +
                "GROUP BY hits.app, hits.uri\n" +
                "ORDER BY COUNT(ip) DESC";
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> createViewStats(rs));
    }

    private ViewStatsDTO createViewStats(ResultSet rs) throws SQLException {
        return ViewStatsDTO.builder()
                .app(rs.getString("app"))
                .uri(rs.getString("uri"))
                .hits(rs.getInt("hits"))
                .build();
    }

}