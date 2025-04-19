package com.pinboard.demo.pattern.observer;

/**
 * Interface para o Subject no padrão Observer
 */
public interface PinSubject {
  void registerObserver(PinObserver observer);
  void removeObserver(PinObserver observer);
  void notifyObservers(String message, Long pinId);
}