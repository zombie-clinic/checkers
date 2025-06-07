package com.example.checkers.persistence;

import com.example.checkers.domain.Game;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {

  List<Game> findAllByProgressIn(List<String> progressList);

  Optional<Game> findGameById(String id);
}
