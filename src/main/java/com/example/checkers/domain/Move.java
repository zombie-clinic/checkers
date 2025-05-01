package com.example.checkers.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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

  private String dark;

  private String light;

  public Move(Game game, Player player, String side, String move, String dark, String light) {
    this.game = game;
    this.player = player;
    this.side = side;
    this.move = move;
    this.dark = dark;
    this.light = light;
  }
}
