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

    // TODO Change to enum, in database as well
    @Default
    private String progress = GameProgress.LOBBY.toString();

    @ManyToOne
    @JoinColumn
    private Player playerOne;
    
    @ManyToOne
    @JoinColumn
    private Player playerTwo;
}
