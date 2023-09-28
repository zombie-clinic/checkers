package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@Getter
@Setter
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn
    private Game game;

    private String side;

    private String state;

    private String move;

    public Move(Game game, String side, String state, String move) {
        this.game = game;
        this.side = side;
        this.state = state;
        this.move = move;
    }
}
