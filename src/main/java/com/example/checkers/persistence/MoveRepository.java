package com.example.checkers.persistence;

import com.example.checkers.domain.Move;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoveRepository extends JpaRepository<Move, String> {

  List<Move> findAllByGameId(String id);
}
