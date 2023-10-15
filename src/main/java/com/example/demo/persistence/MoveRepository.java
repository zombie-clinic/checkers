package com.example.demo.persistence;

import com.example.demo.domain.Move;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoveRepository extends JpaRepository<Move, String> {

    List<Move> findAllByGameId(String id);
}
