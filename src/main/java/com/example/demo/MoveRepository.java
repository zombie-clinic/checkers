package com.example.demo;

import com.example.demo.domain.Game;
import com.example.demo.domain.Move;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MoveRepository extends JpaRepository<Move, String> {

}
