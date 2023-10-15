package com.example.demo.persistence;

import com.example.demo.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface GameRepository extends JpaRepository<Game, String> {

    List<Game> findAllByProgressIn(List<String> progressList);

    Optional<Game> findGameById(String id);
}
