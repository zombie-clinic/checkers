package com.example.checkers.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String progress;

    @ManyToOne
    @JoinColumn
    private Player player;
}
