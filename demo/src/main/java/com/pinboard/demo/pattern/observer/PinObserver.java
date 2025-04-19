package com.pinboard.demo.pattern.observer;

/**
 * Interface Observer para o padrão Observer
 */
public interface PinObserver {
  void update(String message, Long pinId);
}