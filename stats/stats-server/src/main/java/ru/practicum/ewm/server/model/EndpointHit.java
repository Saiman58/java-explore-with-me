package ru.practicum.ewm.server.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "endpoint_hits")
public class EndpointHit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app", nullable = false, length = 255)
    private String app;

    @Column(name = "uri", nullable = false, length = 2048)
    private String uri;


    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    @NotNull(message = "Временная метка не может быть null")
    @PastOrPresent(message = "Временная метка должна быть в прошлом или настоящем")
    private LocalDateTime timestamp;
}
