package com.example.checkers.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;

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
