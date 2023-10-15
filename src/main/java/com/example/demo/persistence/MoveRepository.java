package com.example.demo.persistence;

import com.example.demo.domain.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface MoveRepository extends JpaRepository<Move, String> {

    List<Move> findAllByGameId(String id);
}
