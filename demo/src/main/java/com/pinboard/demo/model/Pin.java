package com.pinboard.demo.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
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
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Pin implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  @Column(length = 1000)
  private String description;

  private String imageUrl;

  private int likes;

  private LocalDateTime createdAt;

  @ManyToOne
  private User creator;

  @ManyToMany
  @ToString.Exclude // Exclui coleção do ToString para evitar recursão
  private Set<Board> boards = new HashSet<>();

  private boolean featured;

  // Método para adicionar um board ao pin
  public void addToBoard(Board board) {
    this.boards.add(board);
    board.getPins().add(this);
  }

  // Método para remover um board do pin
  public void removeFromBoard(Board board) {
    this.boards.remove(board);
    board.getPins().remove(this);
  }

  // Override do método equals para evitar recursão infinita
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Pin pin = (Pin) o;
    return id != null && Objects.equals(id, pin.id);
  }

  // Override do método hashCode para evitar recursão infinita
  @Override
  public int hashCode() {
    // Usamos um valor constante para entidades não persistidas
    return id == null ? 31 : Objects.hash(id);
  }
}