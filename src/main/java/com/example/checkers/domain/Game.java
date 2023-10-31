package com.example.checkers.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder.Default;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Default
    private String progress = GameProgress.STARTING.toString();

    @ManyToOne
    @JoinColumn
    private Player player;
}
