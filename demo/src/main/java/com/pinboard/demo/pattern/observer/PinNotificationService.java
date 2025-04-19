package com.pinboard.demo.pattern.observer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Implementação concreta do Subject para o padrão Observer
 */
@Service
public class PinNotificationService implements PinSubject {

  private List<PinObserver> observers = new ArrayList<>();

  @Override
  public void registerObserver(PinObserver observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(PinObserver observer) {
    observers.remove(observer);
  }

  @Override
  public void notifyObservers(String message, Long pinId) {
    for (PinObserver observer : observers) {
      observer.update(message, pinId);
    }
  }
}