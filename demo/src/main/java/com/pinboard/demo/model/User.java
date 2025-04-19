package com.pinboard.demo.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "app_user") // evita conflito com 'user' palavra reservada em alguns bancos
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;

  private String email;

  private String password; // em produção, usaríamos hash

  private String profileImageUrl;

  private String bio;

  @OneToMany(mappedBy = "creator")
  @ToString.Exclude
  private Set<Pin> pins = new HashSet<>();

  @OneToMany(mappedBy = "owner")
  @ToString.Exclude
  private Set<Board> boards = new HashSet<>();

  @ManyToMany
  @ToString.Exclude
  private Set<User> following = new HashSet<>();

  @ManyToMany(mappedBy = "following")
  @ToString.Exclude
  private Set<User> followers = new HashSet<>();

  @ManyToMany
  @JoinTable(name = "user_favorite_pins", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "pin_id"))
  @ToString.Exclude
  private Set<Pin> favoritePins = new HashSet<>();

  @ManyToMany
  @JoinTable(name = "user_saved_pins", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "pin_id"))
  @ToString.Exclude
  private Set<Pin> savedPins = new HashSet<>();

  @ManyToMany
  @JoinTable(name = "user_liked_pins", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "pin_id"))
  @ToString.Exclude
  private Set<Pin> likedPins = new HashSet<>();

  // Método para seguir outro usuário
  public void follow(User user) {
    following.add(user);
    user.getFollowers().add(this);
  }

  // Método para deixar de seguir outro usuário
  public void unfollow(User user) {
    following.remove(user);
    user.getFollowers().remove(this);
  }

  // Método para favoritar um pin
  public void favoritePin(Pin pin) {
    favoritePins.add(pin);
  }

  // Método para desfavoritar um pin
  public void unfavoritePin(Pin pin) {
    favoritePins.remove(pin);
  }

  // Método para verificar se um pin está favoritado
  public boolean hasFavorite(Pin pin) {
    return favoritePins.contains(pin);
  }

  // Método para salvar um pin
  public void savePin(Pin pin) {
    savedPins.add(pin);
  }

  // Método para remover um pin salvo
  public void unsavePin(Pin pin) {
    savedPins.remove(pin);
  }

  // Método para verificar se um pin está salvo
  public boolean hasSaved(Pin pin) {
    return savedPins.contains(pin);
  }

  // Método para curtir um pin
  public void likePin(Pin pin) {
    likedPins.add(pin);
  }

  // Método para descurtir um pin
  public void unlikePin(Pin pin) {
    likedPins.remove(pin);
  }

  // Método para verificar se um pin foi curtido pelo usuário
  public boolean hasLiked(Pin pin) {
    return likedPins.contains(pin);
  }

  // Override do método equals para evitar recursão infinita
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return id != null && Objects.equals(id, user.id);
  }

  // Override do método hashCode para evitar recursão infinita
  @Override
  public int hashCode() {
    // Usa um valor constante para entidades não persistidas
    return id == null ? 31 : Objects.hash(id);
  }
}