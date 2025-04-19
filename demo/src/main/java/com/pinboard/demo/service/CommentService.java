package com.pinboard.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pinboard.demo.model.Comment;
import com.pinboard.demo.model.Pin;
import com.pinboard.demo.model.User;
import com.pinboard.demo.pattern.observer.PinNotificationService;
import com.pinboard.demo.repository.CommentRepository;
import com.pinboard.demo.repository.PinRepository;

@Service
public class CommentService {

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private PinRepository pinRepository;

  @Autowired
  private PinNotificationService notificationService;

  public List<Comment> getCommentsByPin(Long pinId) {
    return commentRepository.findByPin_IdOrderByCreatedAtDesc(pinId);
  }

  public long countCommentsByPin(Long pinId) {
    return commentRepository.countByPin_Id(pinId);
  }

  public Comment getCommentById(Long commentId) {
    return commentRepository.findById(commentId).orElse(null);
  }

  @Transactional
  public Comment addComment(Long pinId, User author, String content) {
    Pin pin = pinRepository.findById(pinId).orElseThrow();

    Comment comment = new Comment();
    comment.setContent(content);
    comment.setAuthor(author);
    comment.setPin(pin);
    comment.setCreatedAt(LocalDateTime.now());

    Comment savedComment = commentRepository.save(comment);

    // Notifica os observadores sobre o novo comentário
    notificationService.notifyObservers("Novo comentário adicionado ao pin: " + pin.getTitle(), pin.getId());

    return savedComment;
  }

  @Transactional
  public Comment updateComment(Long commentId, String newContent, User requestingUser) {
    Comment comment = commentRepository.findById(commentId).orElseThrow();

    // Verifica se o usuário que está tentando editar é o autor do comentário
    if (requestingUser.getId().equals(comment.getAuthor().getId())) {
      comment.setContent(newContent);

      Comment savedComment = commentRepository.save(comment);

      // Notifica os observadores sobre a edição do comentário
      notificationService.notifyObservers("Comentário editado no pin: " + comment.getPin().getTitle(), comment.getPin().getId());

      return savedComment;
    }

    return null; // Retorna null se o usuário não tiver permissão
  }

  @Transactional
  public void deleteComment(Long commentId, User requestingUser) {
    Comment comment = commentRepository.findById(commentId).orElseThrow();

    // Verifica se o usuário que está tentando excluir é o autor do comentário
    if (requestingUser.getId().equals(comment.getAuthor().getId())) {
      commentRepository.delete(comment);

      // Notifica os observadores sobre a exclusão do comentário
      notificationService.notifyObservers("Comentário removido do pin: " + comment.getPin().getTitle(), comment.getPin().getId());
    }
  }

  @Transactional
  public boolean toggleLikeComment(Long commentId, User user) {
    Comment comment = commentRepository.findById(commentId).orElseThrow();

    boolean wasLiked = comment.isLikedByUser(user);

    if (wasLiked) {
      comment.removeLike(user);
      notificationService.notifyObservers("Usuário removeu like de um comentário no pin: " + comment.getPin().getTitle(), comment.getPin().getId());
    } else {
      comment.addLike(user);
      notificationService.notifyObservers("Usuário curtiu um comentário no pin: " + comment.getPin().getTitle(), comment.getPin().getId());
    }

    commentRepository.save(comment);

    // Retorna o novo estado (true se agora está curtido, false se não está curtido)
    return !wasLiked;
  }

  /**
   * Sincroniza o contador de likes para todos os comentários.
   * Útil para manutenção do banco ou para correção em caso de inconsistência.
   */
  @Transactional
  public void syncAllCommentLikeCounters() {
    List<Comment> comments = commentRepository.findAll();
    for (Comment comment : comments) comment.syncLikesCount();
    commentRepository.saveAll(comments);
  }

  public boolean isCommentLikedByUser(Long commentId, User user) {
    Comment comment = commentRepository.findById(commentId).orElse(null);
    if (comment == null || user == null) return false;

    return comment.isLikedByUser(user);
  }
}