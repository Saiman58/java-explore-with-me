package ru.practicum.explorewithme.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String annotation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Embedded
    private EventLocation location;

    @Column(nullable = false)
    @Builder.Default
    private Boolean paid = false;

    @Column
    @Builder.Default
    private Integer participantLimit = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventState state = EventState.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column
    private LocalDateTime publishedOn;

    @Column(nullable = false, length = 120)
    private String title;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private Set<Request> requests;
}
