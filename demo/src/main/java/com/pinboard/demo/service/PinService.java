package com.pinboard.demo.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pinboard.demo.model.Board;
import com.pinboard.demo.model.Pin;
import com.pinboard.demo.model.User;
import com.pinboard.demo.pattern.decorator.BasicPin;
import com.pinboard.demo.pattern.decorator.FeaturedPinDecorator;
import com.pinboard.demo.pattern.decorator.PinComponent;
import com.pinboard.demo.pattern.decorator.SocialSharingDecorator;
import com.pinboard.demo.pattern.observer.PinNotificationService;
import com.pinboard.demo.pattern.strategy.SortingStrategy;
import com.pinboard.demo.repository.PinRepository;
import com.pinboard.demo.repository.UserRepository;

@Service
public class PinService {

  @Autowired
  private PinRepository pinRepository;

  @Autowired
  private PinNotificationService notificationService;

  @Autowired
  private UserRepository userRepository;

  private SortingStrategy sortingStrategy;

  public void setSortingStrategy(SortingStrategy sortingStrategy) {
    this.sortingStrategy = sortingStrategy;
  }

  public List<Pin> getAllPins() {
    List<Pin> pins = pinRepository.findAll();
    if (sortingStrategy != null)  return sortingStrategy.sort(pins);
    return pins;
  }

  public Pin getPin(Long id) {
    return pinRepository.findById(id).orElse(null);
  }

  public Pin createPin(Pin pin, User creator) {
    pin.setCreator(creator);
    pin.setCreatedAt(LocalDateTime.now());
    Pin savedPin = pinRepository.save(pin);

    // Notifica os observadores sobre a criação do novo pin
    notificationService.notifyObservers("Novo pin criado: " + pin.getTitle(), savedPin.getId());

    return savedPin;
  }

  public Pin updatePin(Pin pin) {
    Pin existingPin = pinRepository.findById(pin.getId()).orElse(null);
    if (existingPin != null) {
      // Atualiza apenas campos editáveis
      existingPin.setTitle(pin.getTitle());
      existingPin.setDescription(pin.getDescription());
      existingPin.setFeatured(pin.isFeatured());

      // Notifica os observadores sobre a atualização do pin
      notificationService.notifyObservers("Pin atualizado: " + pin.getTitle(), pin.getId());

      return pinRepository.save(existingPin);
    }
    return null;
  }

  @Transactional
  public void deletePin(Long id) {
    Pin pin = pinRepository.findById(id).orElse(null);
    if (pin != null) {
      // 1. Remove o pin de todos os usuários que salvaram ele
      List<User> usersWithSavedPin = userRepository.findBySavedPinsContains(pin);
      for (User user : usersWithSavedPin) user.getSavedPins().remove(pin);

      // 2. Remove o pin de todos os usuários que o favoritaram
      List<User> usersWithFavoritedPin = userRepository.findByFavoritePinsContains(pin);
      for (User user : usersWithFavoritedPin) user.getFavoritePins().remove(pin);

      // 3. Remover o pin de todos os usuários que o curtiram
      List<User> usersWithLikedPin = userRepository.findByLikedPinsContains(pin);
      for (User user : usersWithLikedPin) user.getLikedPins().remove(pin);

      // 4. Remover o pin de todos os boards
      for (Board board : new HashSet<>(pin.getBoards())) pin.removeFromBoard(board);

      // 5. Salva todas as alterações
      userRepository.saveAll(usersWithSavedPin);
      userRepository.saveAll(usersWithFavoritedPin);
      userRepository.saveAll(usersWithLikedPin);

      // 6. Finalmente exclui o pin
      pinRepository.delete(pin);

      // Notifica os observadores sobre a exclusão do pin
      notificationService.notifyObservers("Pin excluído: " + pin.getTitle(), pin.getId());
    }
  }

  public void likePin(Long id, User user) {
    Pin pin = pinRepository.findById(id).orElse(null);
    if (pin != null && user != null) {
      // Verifica se o usuário já curtiu o pin
      if (user.hasLiked(pin)) {
        // Remove o like se o usuário já curtiu o pin
        user.unlikePin(pin);
        if (pin.getLikes() > 0) pin.setLikes(pin.getLikes() - 1);
        // Notifica os observadores sobre a remoção do like
        notificationService.notifyObservers("Pin teve um like removido", pin.getId());
      } else {
        // Adiciona o like se o usuário ainda não curtiu o pin
        user.likePin(pin);
        pin.setLikes(pin.getLikes() + 1);
        // Notifica os observadores sobre o novo like
        notificationService.notifyObservers("Pin recebeu um like", pin.getId());
      }
      pinRepository.save(pin);
    }
  }

  // Método que utiliza o padrão Decorator para renderizar um pin conforme suas características
  public String renderPin(Pin pin) {
    PinComponent basicPin = new BasicPin(pin.getTitle(), pin.getImageUrl(), pin.getLikes());

    // Se o pin for destacado, aplica o decorator de destaque
    if (pin.isFeatured()) basicPin = new FeaturedPinDecorator(basicPin);

    // Adiciona o decorator de compartilhamento social em todos os pins
    basicPin = new SocialSharingDecorator(basicPin);

    return basicPin.render();
  }

  public List<Pin> findPinsByUser(User user) {
    return pinRepository.findByCreator(user);
  }

  public List<Pin> findFeaturedPins() {
    return pinRepository.findByFeaturedTrue();
  }

  public List<Pin> searchPins(String keyword) {
    return pinRepository.findByTitleContainingIgnoreCase(keyword);
  }
}