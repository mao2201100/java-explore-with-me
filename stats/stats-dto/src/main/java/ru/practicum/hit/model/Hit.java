package ru.practicum.hit.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hits")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Hit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app")
    private String app; // Идентификатор сервиса для которого записывается информация

    @Column(name = "uri")
    private String uri;  // URI для которого был осуществлен запрос",

    @Column(name = "ip")
    private String ip;  // IP-адрес пользователя, осуществившего запрос

    @Column(name = "created_date")
    private LocalDateTime timestamp; // Дата и время, когда был совершен запрос к эндпоинту (в формате \"yyyy-MM-dd HH:mm:ss\")

}