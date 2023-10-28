package com.example.checkers.persistence;

import com.example.checkers.domain.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface MoveRepository extends JpaRepository<Move, String> {

    List<Move> findAllByGameId(String id);
}
