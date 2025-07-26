package com.example.checkers.adapters.db;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface GameRepository extends JpaRepository<PersistentGame, String> {

  List<PersistentGame> findAllByProgressIn(List<String> progressList);

  Optional<PersistentGame> findGameById(String id);
}
