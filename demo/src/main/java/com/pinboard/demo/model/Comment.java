package com.pinboard.demo.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String content;

    private LocalDateTime createdAt;

    @ManyToOne
    private User author;

    @ManyToOne
    private Pin pin;
    
    // Campo para armazenar o contador de likes (para melhor performance)
    private int likesCount = 0;
    
    @ManyToMany
    @JoinTable(name = "comment_liked_users", 
               joinColumns = @JoinColumn(name = "comment_id"), 
               inverseJoinColumns = @JoinColumn(name = "user_id"))
    @ToString.Exclude
    private Set<User> likedByUsers = new HashSet<>();
    
    // Método para adicionar um like de um usuário
    public void addLike(User user) {
        likedByUsers.add(user);
        this.likesCount = likedByUsers.size();
    }
    
    // Método para remover um like de um usuário
    public void removeLike(User user) {
        likedByUsers.remove(user);
        this.likesCount = likedByUsers.size();
    }
    
    // Método para verificar se um usuário já curtiu este comentário
    public boolean isLikedByUser(User user) {
        return likedByUsers.contains(user);
    }
    
    // Método para obter a contagem total de likes (agora retorna diretamente o campo)
    public int getLikesCount() {
        return this.likesCount;
    }
    
    // Método para sincronizar o contador de likes com o tamanho da coleção de usuários
    public void syncLikesCount() {
        this.likesCount = this.likedByUsers.size();
    }

    // Override do método equals para evitar recursão infinita
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return id != null && Objects.equals(id, comment.id);
    }

    // Override do método hashCode para evitar recursão infinita
    @Override
    public int hashCode() {
        // Usa um valor constante para entidades não persistidas
        return id == null ? 31 : Objects.hash(id);
    }
}