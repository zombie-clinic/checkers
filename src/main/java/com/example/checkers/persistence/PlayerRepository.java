package com.example.checkers.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.example.checkers.domain.Player;

@RepositoryRestResource(exported = false)
public interface PlayerRepository extends JpaRepository<Player, Long> {

}
