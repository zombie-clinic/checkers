package com.example.checkers.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  // TODO Change to enum, in database as well
  @Default
  private String progress = GameProgress.LOBBY.toString();

  @ManyToOne
  @JoinColumn
  private Player playerOne;

  @ManyToOne
  @JoinColumn
  private Player playerTwo;

  // fixme find simpler way to store starting state
  // todo how to pass default value into the db? Currently populated on creation
  @Default
  private String startingState = String.format("{\"dark\":[%s]\"light\":[%s],\"kings\":[%s]}",
      Checkerboard.getStartingState().getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
      Checkerboard.getStartingState().getLight().stream().map(String::valueOf).collect(Collectors.joining(",")),
      Stream.of().map(String::valueOf).collect(Collectors.joining(","))
  );
}
