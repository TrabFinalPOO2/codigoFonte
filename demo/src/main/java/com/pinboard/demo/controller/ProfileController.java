package com.pinboard.demo.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pinboard.demo.model.User;
import com.pinboard.demo.service.PinService;
import com.pinboard.demo.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

@Controller
@RequestMapping("/profile")
public class ProfileController {

  @Autowired
  private UserService userService;

  @Autowired
  private PinService pinService;

  // Exibe o perfil do usuário logado
  @GetMapping("")
  public String currentUserProfile(HttpSession session, Model model) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/auth/login";

    return "redirect:/profile/" + currentUser.getUsername();
  }

  // Exibe o perfil de um usuário pelo username
  @GetMapping("/{username}")
  @Transactional
  public String viewUserProfile(@PathVariable String username,
                                @RequestParam(required = false) String tab,
                                HttpSession session,
                                Model model) {
    Optional<User> userOpt = userService.getUserByUsername(username);
    if (userOpt.isEmpty()) return "error/404";

    User user = userOpt.get();

    // Carrega de forma explícita as coleções lazy para evitar LazyInitializationException
    user.getPins().size(); 
    user.getBoards().size();
    user.getFollowers().size();
    user.getFollowing().size();

    model.addAttribute("profileUser", user);
    model.addAttribute("title", user.getUsername() + " - Perfil");

    // Adiciona informações para a aba ativa baseada no parâmetro tab
    if (tab != null) {
      switch (tab) {
        case "saved" -> {
          model.addAttribute("activeTab", "saved");
          user.getSavedPins().size(); // Carrega explicitamente a coleção
        }
        case "favorites" -> {
          model.addAttribute("activeTab", "favorites");
          user.getFavoritePins().size(); // Carrega explicitamente a coleção
        }
        case "boards" -> model.addAttribute("activeTab", "boards");
        default -> model.addAttribute("activeTab", "pins");
      }
    } else model.addAttribute("activeTab", "pins");

    // Verifica se o usuário atual está seguindo o perfil
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser != null) {
      // Recarrega o usuário atual para ter acesso às coleções em uma transação ativa
      currentUser = userService.refreshUser(currentUser.getId());
      model.addAttribute("isFollowing", currentUser.getFollowing().contains(user));
      model.addAttribute("isOwnProfile", currentUser.getId().equals(user.getId()));

      // Atualiza o usuário na sessão com a versão mais recente
      session.setAttribute("currentUser", currentUser);
    } else {
      model.addAttribute("isFollowing", false);
      model.addAttribute("isOwnProfile", false);
    }

    return "profile/show";
  }

  // Exibe os pins favoritos do usuário
  @GetMapping("/{username}/favorites")
  @Transactional
  public String viewUserFavorites(@PathVariable String username,
      HttpSession session,
      Model model) {

    Optional<User> userOpt = userService.getUserByUsername(username);
    if (userOpt.isEmpty()) return "error/404";

    User user = userOpt.get();

    // Carrega de forma explícita as coleções lazy
    user.getFavoritePins().size();
    user.getFollowers().size();
    user.getFollowing().size();

    model.addAttribute("profileUser", user);
    model.addAttribute("title", user.getUsername() + " - Favoritos");

    // Define a aba ativa
    model.addAttribute("activeTab", "favorites");

    // Verifica se o usuário atual está seguindo o perfil
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser != null) {
      // Recarrega o usuário atual para ter acesso às coleções em uma transação ativa
      currentUser = userService.refreshUser(currentUser.getId());
      model.addAttribute("isFollowing", currentUser.getFollowing().contains(user));
      model.addAttribute("isOwnProfile", currentUser.getId().equals(user.getId()));

      // Atualiza o usuário na sessão com a versão mais recente
      session.setAttribute("currentUser", currentUser);
    } else {
      model.addAttribute("isFollowing", false);
      model.addAttribute("isOwnProfile", false);
    }

    return "profile/show";
  }

  // Exibe os pins salvos do usuário
  @GetMapping("/{username}/saved")
  @Transactional
  public String viewUserSaved(@PathVariable String username,
      HttpSession session,
      Model model) {

    Optional<User> userOpt = userService.getUserByUsername(username);
    if (userOpt.isEmpty()) return "error/404";

    User user = userOpt.get();

    // Carrega de forma explícita as coleções lazy
    user.getSavedPins().size();
    user.getFollowers().size();
    user.getFollowing().size();

    model.addAttribute("profileUser", user);
    model.addAttribute("title", user.getUsername() + " - Salvos");

    // Define a aba ativa
    model.addAttribute("activeTab", "saved");

    // Verifica se o usuário atual está seguindo este perfil
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser != null) {
      // Recarrega o usuário atual para ter acesso às coleções em uma transação ativa
      currentUser = userService.refreshUser(currentUser.getId());
      model.addAttribute("isFollowing", currentUser.getFollowing().contains(user));
      model.addAttribute("isOwnProfile", currentUser.getId().equals(user.getId()));

      // Atualiza o usuário na sessão com a versão mais recente
      session.setAttribute("currentUser", currentUser);
    } else {
      model.addAttribute("isFollowing", false);
      model.addAttribute("isOwnProfile", false);
    }

    return "profile/show";
  }

  // Segue um usuário
  @PostMapping("/follow/{username}")
  @Transactional
  public String followUser(@PathVariable String username,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/auth/login";

    // Recarrega o usuário atual para ter certeza de que é a versão mais recente
    currentUser = userService.refreshUser(currentUser.getId());

    Optional<User> userToFollowOpt = userService.getUserByUsername(username);
    if (userToFollowOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "Usuário não encontrado");
      return "redirect:/";
    }

    User userToFollow = userToFollowOpt.get();

    // Evita que um usuário siga a si mesmo
    if (currentUser.getId().equals(userToFollow.getId())) {
      redirectAttributes.addFlashAttribute("error", "Você não pode seguir a si mesmo");
      return "redirect:/profile/" + username;
    }

    // Verifica se já está seguindo
    if (currentUser.getFollowing().contains(userToFollow)) currentUser.unfollow(userToFollow);
    else currentUser.follow(userToFollow);

    userService.saveUser(currentUser);

    // Atualiza o usuário na sessão
    session.setAttribute("currentUser", currentUser);

    return "redirect:/profile/" + username;
  }

  // Exibe o formulário de edição do perfil
  @GetMapping("/edit")
  public String showEditProfileForm(HttpSession session, Model model) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/auth/login";

    // Recarrega o usuário para ter certeza de que é a versão mais recente
    currentUser = userService.refreshUser(currentUser.getId());

    model.addAttribute("user", currentUser);
    model.addAttribute("title", "Editar Perfil");

    return "profile/edit";
  }

  // Processa a atualização do perfil
  @PostMapping("/update")
  @Transactional
  public String updateProfile(@ModelAttribute User user,
      HttpSession session,
      RedirectAttributes redirectAttributes) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/auth/login";

    // Recarrega o usuário atual para ter a versão mais recente
    currentUser = userService.refreshUser(currentUser.getId());

    // Verifica se o nome de usuário já está em uso por outro usuário
    if (!currentUser.getUsername().equals(user.getUsername())) {
      if (userService.getUserByUsername(user.getUsername()).isPresent()) {
        redirectAttributes.addFlashAttribute("error", "Este nome de usuário já está em uso");
        return "redirect:/profile/edit";
      }
    }

    // Mantém os dados que não devem ser alterados pelo formulário
    user.setId(currentUser.getId());
    user.setEmail(currentUser.getEmail()); // Não permite alteração de email neste formulário
    user.setPassword(currentUser.getPassword()); // Não altera senha aqui
    user.setBoards(currentUser.getBoards());
    user.setPins(currentUser.getPins());
    user.setFavoritePins(currentUser.getFavoritePins());
    user.setSavedPins(currentUser.getSavedPins());
    user.setFollowers(currentUser.getFollowers());
    user.setFollowing(currentUser.getFollowing());

    // Salva as alterações
    User updatedUser = userService.saveUser(user);

    // Atualiza a sessão com o usuário atualizado
    session.setAttribute("currentUser", updatedUser);

    redirectAttributes.addFlashAttribute("success", "Perfil atualizado com sucesso");
    return "redirect:/profile";
  }

  // Exibe o formulário de alteração de senha
  @GetMapping("/change-password")
  public String showChangePasswordForm(HttpSession session, Model model) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/auth/login";

    model.addAttribute("title", "Alterar Senha");
    return "profile/change-password";
  }

  // Processa a alteração de senha
  @PostMapping("/update-password")
  public String updatePassword(@RequestParam String currentPassword,
      @RequestParam String newPassword,
      @RequestParam String confirmPassword,
      HttpSession session,
      RedirectAttributes redirectAttributes) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/auth/login";

    // Recarrega o usuário para ter certeza de que é a versão mais recente
    currentUser = userService.refreshUser(currentUser.getId());

    // Verifica se a senha atual está correta usando BCrypt
    if (!BCrypt.checkpw(currentPassword, currentUser.getPassword())) {
      redirectAttributes.addFlashAttribute("error", "Senha atual incorreta");
      return "redirect:/profile/change-password";
    }

    // Verifica se a nova senha e a confirmação correspondem
    if (!newPassword.equals(confirmPassword)) {
      redirectAttributes.addFlashAttribute("error", "A nova senha e a confirmação não correspondem");
      return "redirect:/profile/change-password";
    }

    // Verifica a força da senha (pelo menos 8 caracteres)
    if (newPassword.length() < 8) {
      redirectAttributes.addFlashAttribute("error", "A nova senha deve ter pelo menos 8 caracteres");
      return "redirect:/profile/change-password";
    }

    // Gera hash da nova senha e atualiza o usuário
    String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
    currentUser.setPassword(hashedPassword);
    userService.saveUser(currentUser);

    // Força logout para que o usuário faça login novamente com a nova senha
    session.invalidate();

    redirectAttributes.addFlashAttribute("success", "Senha alterada com sucesso. Por favor, faça login novamente.");
    return "redirect:/auth/login";
  }
}