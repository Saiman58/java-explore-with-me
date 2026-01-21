package ru.practicum.explorewithme.server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation;

    @Column(name = "description", length = 7000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Embedded
    private Location location;

    @Column(name = "paid")
    @Builder.Default
    private Boolean paid = false;

    @Column(name = "participant_limit")
    @Builder.Default
    private Integer participantLimit = 0;

    @Column(name = "request_moderation")
    @Builder.Default
    private Boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 20)
    @Builder.Default
    private EventState state = EventState.PENDING;

    @PrePersist
    protected void onCreate() {
        createdOn = LocalDateTime.now();
    }
}