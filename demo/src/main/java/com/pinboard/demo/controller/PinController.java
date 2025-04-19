package com.pinboard.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pinboard.demo.model.Pin;
import com.pinboard.demo.model.User;
import com.pinboard.demo.pattern.singleton.ConfigurationManager;
import com.pinboard.demo.pattern.strategy.DateStrategy;
import com.pinboard.demo.pattern.strategy.PopularityStrategy;
import com.pinboard.demo.pattern.strategy.SortingStrategy;
import com.pinboard.demo.service.BoardService;
import com.pinboard.demo.service.PinService;
import com.pinboard.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/pins")
public class PinController {

  @Autowired
  private PinService pinService;

  @Autowired
  private BoardService boardService;

  @Autowired
  private UserService userService;

  // Obtém a instância do Singleton para configurações
  private final ConfigurationManager configManager = ConfigurationManager.getInstance();

  @GetMapping
  public String getAllPins(Model model, @RequestParam(required = false) String sort) {
    // Utiliza o padrão Strategy para ordenação dos pins
    SortingStrategy strategy = null;

    if ("popular".equals(sort)) strategy = new PopularityStrategy();
    else if ("date".equals(sort)) strategy = new DateStrategy();

    if (strategy != null) pinService.setSortingStrategy(strategy);

    List<Pin> pins = pinService.getAllPins();

    model.addAttribute("pins", pins);
    model.addAttribute("appName", configManager.getAppName());
    model.addAttribute("version", configManager.getVersion());

    return "pins/index";
  }

  @GetMapping("/{id}")
  @Transactional
  public String getPin(@PathVariable Long id, Model model, HttpSession session) {
    Pin pin = pinService.getPin(id);
    if (pin == null) return "redirect:/pins";

    model.addAttribute("pin", pin);
    model.addAttribute("pinHtml", pinService.renderPin(pin));
    model.addAttribute("appName", configManager.getAppName());

    // Verifica se o usuário logado favoritou/salvou/curtiu o pin
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser != null) {
      // Recarrega o usuário para ter acesso às coleções lazy em uma transação ativa
      currentUser = userService.refreshUser(currentUser.getId());

      // Carrega explicitamente os boards do usuário para o modal de adicionar a board
      currentUser.getBoards().size();

      // Usa os métodos transacionais do service
      boolean isFavorited = userService.hasUserFavoritedPin(currentUser.getId(), id);
      boolean isSaved = userService.hasUserSavedPin(currentUser.getId(), id);
      boolean isLiked = userService.hasUserLikedPin(currentUser.getId(), id);

      model.addAttribute("isFavorited", isFavorited);
      model.addAttribute("isSaved", isSaved);
      model.addAttribute("isLiked", isLiked);

      // Atualiza o usuário na sessão com a versão mais recente
      session.setAttribute("currentUser", currentUser);
    }

    return "pins/show";
  }

  @GetMapping("/new")
  public String newPinForm(Model model, HttpSession session) {
    // Verifica se o usuário está logado
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/auth/login";

    model.addAttribute("pin", new Pin());
    model.addAttribute("appName", configManager.getAppName());
    return "pins/new";
  }

  @PostMapping
  public String createPin(@ModelAttribute Pin pin, HttpSession session) {
    // Obtém o usuário da sessão
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/auth/login";

    pinService.createPin(pin, currentUser);
    return "redirect:/pins";
  }

  @GetMapping("/{id}/edit")
  public String editPinForm(@PathVariable Long id, Model model, HttpSession session) {
    // Verifica se o usuário está logado
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/auth/login";

    Pin pin = pinService.getPin(id);
    if (pin == null) return "redirect:/pins";

    // Verifica se o usuário é o criador do pin
    if (!pin.getCreator().getId().equals(currentUser.getId())) return "redirect:/pins/" + id;

    model.addAttribute("pin", pin);
    model.addAttribute("appName", configManager.getAppName());
    return "pins/edit";
  }

  @PostMapping("/{id}")
  public String updatePin(@PathVariable Long id, @ModelAttribute Pin pin, HttpSession session) {
    // Verifica se o usuário está logado
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/auth/login";

    Pin existingPin = pinService.getPin(id);
    if (existingPin == null) return "redirect:/pins";

    // Verifica se o usuário é o criador do pin
    if (!existingPin.getCreator().getId().equals(currentUser.getId())) return "redirect:/pins/" + id;

    pin.setId(id);
    pin.setCreator(currentUser);
    pinService.updatePin(pin);
    return "redirect:/pins/" + id;
  }

  @GetMapping("/{id}/delete")
  public String deletePin(@PathVariable Long id, HttpSession session) {
    // Verifica se o usuário está logado
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/auth/login";

    Pin pin = pinService.getPin(id);
    if (pin == null) return "redirect:/pins";

    // Verifica se o usuário é o criador do pin
    if (!pin.getCreator().getId().equals(currentUser.getId())) return "redirect:/pins";

    pinService.deletePin(id);
    return "redirect:/pins";
  }

  @GetMapping("/{id}/like")
  @Transactional
  public String likePin(@PathVariable Long id, HttpSession session) {
    // Verifica se o usuário está logado
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/auth/login";

    // Recarregar o usuário para ter acesso às coleções lazy
    currentUser = userService.refreshUser(currentUser.getId());

    // Atualiza o método para passar o usuário atual
    pinService.likePin(id, currentUser);

    // Atualiza o usuário na sessão para que as alterações de like sejam refletidas
    session.setAttribute("currentUser", currentUser);

    return "redirect:/pins/" + id;
  }

  @GetMapping("/search")
  public String searchPins(@RequestParam String keyword, Model model) {
    List<Pin> pins = pinService.searchPins(keyword);
    model.addAttribute("pins", pins);
    model.addAttribute("keyword", keyword);
    model.addAttribute("appName", configManager.getAppName());
    return "pins/search";
  }

  // Favoritar um pin (AJAX)
  @PostMapping("/{id}/favorite")
  @ResponseBody
  @Transactional
  public ResponseEntity<String> favoritePin(@PathVariable Long id, HttpSession session) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return ResponseEntity.status(401).body("Usuário não autenticado");

    // Recarrega o usuário para ter acesso às coleções lazy
    currentUser = userService.refreshUser(currentUser.getId());

    Pin pin = pinService.getPin(id);
    if (pin == null) return ResponseEntity.status(404).body("Pin não encontrado");

    boolean isFavorited = userService.hasUserFavoritedPin(currentUser.getId(), id);

    if (isFavorited) {
      userService.unfavoritePin(currentUser.getId(), id);
      // Atualiza o usuário na sessão
      session.setAttribute("currentUser", currentUser);
      return ResponseEntity.ok("Pin removido dos favoritos");
    } else {
      userService.favoritePin(currentUser.getId(), id);
      // Atualiza o usuário na sessão
      session.setAttribute("currentUser", currentUser);
      return ResponseEntity.ok("Pin adicionado aos favoritos");
    }
  }

  // Salva um pin (AJAX)
  @PostMapping("/{id}/save")
  @ResponseBody
  @Transactional
  public ResponseEntity<String> savePin(@PathVariable Long id, HttpSession session) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return ResponseEntity.status(401).body("Usuário não autenticado");

    // Recarrega o usuário para ter acesso às coleções lazy
    currentUser = userService.refreshUser(currentUser.getId());

    Pin pin = pinService.getPin(id);
    if (pin == null) return ResponseEntity.status(404).body("Pin não encontrado");

    boolean isSaved = userService.hasUserSavedPin(currentUser.getId(), id);

    if (isSaved) {
      userService.unsavePin(currentUser.getId(), id);
      // Atualiza o usuário na sessão
      session.setAttribute("currentUser", currentUser);
      return ResponseEntity.ok("Pin removido dos salvos");
    } else {
      userService.savePin(currentUser.getId(), id);
      // Atualiza o usuário na sessão
      session.setAttribute("currentUser", currentUser);
      return ResponseEntity.ok("Pin salvo com sucesso");
    }
  }
}