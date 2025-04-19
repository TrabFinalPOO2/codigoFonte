package com.pinboard.demo.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Board {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String description;

  private LocalDateTime createdAt;

  private String coverImageUrl;

  private boolean isPrivate;

  @ManyToOne
  private User owner;

  @ManyToMany(mappedBy = "boards")
  @ToString.Exclude // Exclui pins do ToString para evitar recursão
  private Set<Pin> pins = new HashSet<>();

  // Método para adicionar um pin ao board
  public void addPin(Pin pin) {
    this.pins.add(pin);
    pin.getBoards().add(this);
  }

  // Método para remover um pin do board
  public void removePin(Pin pin) {
    this.pins.remove(pin);
    pin.getBoards().remove(this);
  }

  // Override do método equals para evitar recursão infinita
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Board board = (Board) o;
    return id != null && Objects.equals(id, board.id);
  }

  // Override do método hashCode para evitar recursão infinita
  @Override
  public int hashCode() {
    // Usa um valor constante para entidades não persistidas
    return id == null ? 31 : Objects.hash(id);
  }
}