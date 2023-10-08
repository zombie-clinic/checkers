package com.example.demo;

import com.example.demo.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, String> {

    List<Game> findAllByProgressIn(List<String> progressList);

    Optional<Game> findGameById(String id);
}
