package com.pinboard.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pinboard.demo.model.Comment;
import com.pinboard.demo.model.User;
import com.pinboard.demo.service.CommentService;
import com.pinboard.demo.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/comments")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/pin/{pinId}")
    @ResponseBody
    public List<Comment> getCommentsByPin(@PathVariable Long pinId) {
        return commentService.getCommentsByPin(pinId);
    }
    
    @PostMapping("/pin/{pinId}")
    @Transactional
    public String addComment(
            @PathVariable Long pinId,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Verifica se o usuário está logado
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "É necessário estar logado para comentar");
            return "redirect:/pins/" + pinId;
        }
        
        // Recarrega o usuário para ter a versão mais recente
        currentUser = userService.refreshUser(currentUser.getId());
        
        // Valida o conteúdo do comentário
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "O comentário não pode estar vazio");
            return "redirect:/pins/" + pinId;
        }
        
        // Adiciona o comentário e atualiza a sessão
        commentService.addComment(pinId, currentUser, content);
        session.setAttribute("currentUser", currentUser);
        
        redirectAttributes.addFlashAttribute("success", "Comentário adicionado com sucesso");
        return "redirect:/pins/" + pinId;
    }
    
    @PutMapping("/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<String> updateComment(
            @PathVariable Long id,
            @RequestParam String content,
            HttpSession session) {
        
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Usuário não autenticado");
        }
        
        // Recarrega o usuário para ter acesso às coleções lazy
        currentUser = userService.refreshUser(currentUser.getId());
        
        // Valida o conteúdo do comentário
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("O comentário não pode estar vazio");
        }
        
        try {
            Comment updatedComment = commentService.updateComment(id, content, currentUser);
            
            if (updatedComment == null) {
                return ResponseEntity.status(403).body("Você não tem permissão para editar este comentário");
            }
            
            return ResponseEntity.ok("Comentário atualizado com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar comentário: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<String> deleteComment(
            @PathVariable Long id,
            HttpSession session) {
        
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Usuário não autenticado");
        }
        
        // Recarrega o usuário para ter acesso às coleções lazy
        currentUser = userService.refreshUser(currentUser.getId());
        
        try {
            commentService.deleteComment(id, currentUser);
            return ResponseEntity.ok("Comentário excluído com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao excluir comentário: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/like")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> toggleLikeComment(
            @PathVariable Long id,
            HttpSession session) {
        
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Usuário não autenticado"
            ));
        }
        
        // Recarrega o usuário para ter acesso às coleções lazy
        currentUser = userService.refreshUser(currentUser.getId());
        
        try {
            boolean isNowLiked = commentService.toggleLikeComment(id, currentUser);
            
            // Obtém o comentário atualizado para retornar a contagem de likes
            Comment comment = commentService.getCommentById(id);
            int likesCount = comment.getLikesCount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("liked", isNowLiked);
            response.put("likesCount", likesCount);
            response.put("message", isNowLiked ? "Comentário curtido" : "Curtida removida");
            
            // Atualiza o usuário na sessão
            session.setAttribute("currentUser", currentUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Erro ao curtir comentário: " + e.getMessage()
            ));
        }
    }
}