package com.example.demo.persistence;

import com.example.demo.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface PlayerRepository extends JpaRepository<Player, Long> {

}
