package ru.practicum.explorewithme.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "events")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    private Category category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String annotation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    @ToString.Exclude
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
    @ToString.Exclude
    private Set<Request> requests;
}