package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
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

    @ManyToOne
    @JoinColumn
    private User user;

    private String side;

    private String state;

    private String move;

    public Move(Game game, User user, String side, String state, String move) {
        this.game = game;
        this.user = user;
        this.side = side;
        this.state = state;
        this.move = move;
    }
}
