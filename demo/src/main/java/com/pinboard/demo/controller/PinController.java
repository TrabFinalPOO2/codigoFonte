package com.pinboard.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.pinboard.demo.model.Comment;
import com.pinboard.demo.model.Pin;
import com.pinboard.demo.model.User;
import com.pinboard.demo.pattern.singleton.ConfigurationManager;
import com.pinboard.demo.pattern.strategy.DateStrategy;
import com.pinboard.demo.pattern.strategy.PopularityStrategy;
import com.pinboard.demo.pattern.strategy.SortingStrategy;
import com.pinboard.demo.service.BoardService;
import com.pinboard.demo.service.CommentService;
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
  
  @Autowired
  private CommentService commentService;

  // Obtém a instância do Singleton para configurações
  private final ConfigurationManager configManager = ConfigurationManager.getInstance();

  // Método para download da imagem
  @GetMapping("/{id}/download")
  @ResponseBody
  public ResponseEntity<Object> downloadImage(@PathVariable Long id, HttpSession session) {
    Pin pin = pinService.getPin(id);
    if (pin == null) return ResponseEntity.notFound().build();
    
    String imageUrl = pin.getImageUrl();
    
    try {
      // Cria uma URL a partir da string usando URI
      java.net.URI uri = new java.net.URI(imageUrl);
      java.net.URL url = uri.toURL();
      
      // Abre conexão com a URL
      java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      
      // Obtém o nome do arquivo da URL
      String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
      
      // Se o nome do arquivo não tiver extensão, adicione .jpg
      if (!fileName.contains(".")) fileName += ".jpg";
      
      // Lê os bytes da imagem
      java.io.InputStream inputStream = connection.getInputStream();
      byte[] imageBytes = inputStream.readAllBytes();
      inputStream.close();
      
      // Configura os headers para download
      org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
      headers.setContentDispositionFormData("attachment", fileName);
      headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
      
      // Retorna a resposta com a imagem
      return new ResponseEntity<>(imageBytes, headers, org.springframework.http.HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping
  public String getAllPins(Model model, @RequestParam(required = false) String sort, HttpSession session) {
    // Utiliza o padrão Strategy para ordenação dos pins
    SortingStrategy strategy = null;

    if ("popular".equals(sort)) strategy = new PopularityStrategy();
    else if ("date".equals(sort)) strategy = new DateStrategy();

    if (strategy != null) pinService.setSortingStrategy(strategy);

    List<Pin> pins = pinService.getAllPins();

    // Adicionar informações de pins já salvos pelo usuário atual
    Map<Long, Boolean> savedPinsMap = new HashMap<>();
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser != null) {
      // Recarrega o usuário para ter acesso às coleções lazy
      currentUser = userService.refreshUser(currentUser.getId());
      
      // Monta um mapa de pins salvos para verificação rápida na view
      for (Pin pin : pins) {
        savedPinsMap.put(pin.getId(), userService.hasUserSavedPin(currentUser.getId(), pin.getId()));
      }
      
      // Atualiza o usuário na sessão
      session.setAttribute("currentUser", currentUser);
    }
    
    model.addAttribute("savedPinsMap", savedPinsMap);
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

    // Carrega os comentários do pin
    List<Comment> comments = commentService.getCommentsByPin(id);
    
    model.addAttribute("pin", pin);
    model.addAttribute("pinHtml", pinService.renderPin(pin));
    model.addAttribute("appName", configManager.getAppName());
    model.addAttribute("comments", comments);
    model.addAttribute("commentCount", comments.size());

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
      
      // Mapeia quais comentários o usuário curtiu
      Map<Long, Boolean> likedCommentIds = new HashMap<>();
      for (Comment comment : comments) {
        likedCommentIds.put(comment.getId(), comment.isLikedByUser(currentUser));
      }
      
      model.addAttribute("likedCommentIds", likedCommentIds);
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
  public String searchPins(@RequestParam String keyword, Model model, HttpSession session) {
    List<Pin> pins = pinService.searchPins(keyword);
    
    // Adicionar informações de pins já salvos pelo usuário atual
    Map<Long, Boolean> savedPinsMap = new HashMap<>();
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser != null) {
      // Recarrega o usuário para ter acesso às coleções lazy
      currentUser = userService.refreshUser(currentUser.getId());
      
      // Monta um mapa de pins salvos para verificação rápida na view
      for (Pin pin : pins) {
        savedPinsMap.put(pin.getId(), userService.hasUserSavedPin(currentUser.getId(), pin.getId()));
      }
      
      // Atualiza o usuário na sessão
      session.setAttribute("currentUser", currentUser);
    }
    
    model.addAttribute("savedPinsMap", savedPinsMap);
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