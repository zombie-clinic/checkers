package com.example.checkers.domain;

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
    private Player player;

    private String side;

    private String move;

    private String black;

    private String white;

    public Move(Game game, Player player, String side, String move, String black, String white) {
        this.game = game;
        this.player = player;
        this.side = side;
        this.move = move;
        this.black = black;
        this.white = white;
    }
}
