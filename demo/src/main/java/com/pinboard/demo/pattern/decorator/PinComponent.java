package com.pinboard.demo.pattern.decorator;

/**
 * Interface base para o componente do padrão Decorator
 */
public interface PinComponent {
  String render();
  int getLikes();
}