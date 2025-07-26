package com.example.checkers.adapters.db;

import com.example.checkers.core.Move;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface MoveRepository extends JpaRepository<Move, String> {

  List<Move> findAllByGameId(String id);
}
