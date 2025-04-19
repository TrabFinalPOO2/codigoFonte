package com.pinboard.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pinboard.demo.model.Pin;
import com.pinboard.demo.model.User;
import com.pinboard.demo.repository.PinRepository;
import com.pinboard.demo.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;
  
  @Autowired
  private PinRepository pinRepository;
  
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }
  
  public Optional<User> getUserById(Long id) {
    return userRepository.findById(id);
  }
  
  public Optional<User> getUserByUsername(String username) {
    return userRepository.findByUsername(username);
  }
  
  @Transactional
  public User refreshUser(Long userId) {
    return userRepository.findById(userId).orElseThrow();
  }
  
  public User saveUser(User user) {
    return userRepository.save(user);
  }
  
  @Transactional
  public void favoritePin(Long userId, Long pinId) {
    User user = userRepository.findById(userId).orElseThrow();
    Pin pin = pinRepository.findById(pinId).orElseThrow();
    
    user.favoritePin(pin);
    userRepository.save(user);
  }
  
  @Transactional
  public void unfavoritePin(Long userId, Long pinId) {
    User user = userRepository.findById(userId).orElseThrow();
    Pin pin = pinRepository.findById(pinId).orElseThrow();
    
    user.unfavoritePin(pin);
    userRepository.save(user);
  }
  
  @Transactional
  public void savePin(Long userId, Long pinId) {
    User user = userRepository.findById(userId).orElseThrow();
    Pin pin = pinRepository.findById(pinId).orElseThrow();
    
    user.savePin(pin);
    userRepository.save(user);
  }
  
  @Transactional
  public void unsavePin(Long userId, Long pinId) {
    User user = userRepository.findById(userId).orElseThrow();
    Pin pin = pinRepository.findById(pinId).orElseThrow();
    
    user.unsavePin(pin);
    userRepository.save(user);
  }
  
  @Transactional
  public Set<Pin> getFavoritePins(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    return user.getFavoritePins();
  }
  
  @Transactional
  public Set<Pin> getSavedPins(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    return user.getSavedPins();
  }
  
  @Transactional
  public boolean hasUserFavoritedPin(Long userId, Long pinId) {
    User user = userRepository.findById(userId).orElse(null);
    Pin pin = pinRepository.findById(pinId).orElse(null);
    
    if (user == null || pin == null) return false;
    
    return user.getFavoritePins().contains(pin);
  }
  
  @Transactional
  public boolean hasUserSavedPin(Long userId, Long pinId) {
    User user = userRepository.findById(userId).orElse(null);
    Pin pin = pinRepository.findById(pinId).orElse(null);
    
    if (user == null || pin == null) return false;
    
    return user.getSavedPins().contains(pin);
  }
  
  @Transactional
  public void likePin(Long userId, Long pinId) {
    User user = userRepository.findById(userId).orElseThrow();
    Pin pin = pinRepository.findById(pinId).orElseThrow();
    
    user.likePin(pin);
    userRepository.save(user);
  }
  
  @Transactional
  public void unlikePin(Long userId, Long pinId) {
    User user = userRepository.findById(userId).orElseThrow();
    Pin pin = pinRepository.findById(pinId).orElseThrow();
    
    user.unlikePin(pin);
    userRepository.save(user);
  }
  
  @Transactional
  public Set<Pin> getLikedPins(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    return user.getLikedPins();
  }
  
  @Transactional
  public boolean hasUserLikedPin(Long userId, Long pinId) {
    User user = userRepository.findById(userId).orElse(null);
    Pin pin = pinRepository.findById(pinId).orElse(null);
    
    if (user == null || pin == null) return false;
    
    return user.getLikedPins().contains(pin);
  }
}