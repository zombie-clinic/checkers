package com.example.checkers.core;

import com.example.checkers.model.ClientState;
import com.example.checkers.model.ServerState;
import java.util.Objects;
import java.util.Set;
import javax.validation.Valid;

// todo three different sets should be types
public record State(Set<Integer> dark, Set<Integer> light, Set<Integer> kings) {

  public Set<Integer> getDark() {
    return dark;
  }

  public Set<Integer> getLight() {
    return light;
  }

  public Set<Integer> getKings() {
    return kings;
  }

  public static State from(@Valid ClientState clientState) {
    return new State(clientState.getDark(), clientState.getLight(), clientState.getKings());
  }

  public static ServerState toServerState(@Valid State state) {
    return new ServerState(state.getDark(), state.getLight(), state.getKings());
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    State state = (State) o;
    return Objects.equals(dark, state.dark) && Objects.equals(light, state.light) && Objects.equals(kings, state.kings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dark, light, kings);
  }
}
