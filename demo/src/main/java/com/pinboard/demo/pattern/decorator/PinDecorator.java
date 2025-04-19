package com.pinboard.demo.pattern.decorator;

/**
 * Decorador abstrato que implementa a interface e mantém uma referência ao
 * componente decorado
 */
public abstract class PinDecorator implements PinComponent {
  protected PinComponent decoratedPin;

  public PinDecorator(PinComponent decoratedPin) {
    this.decoratedPin = decoratedPin;
  }

  @Override
  public String render() {
    return decoratedPin.render();
  }

  @Override
  public int getLikes() {
    return decoratedPin.getLikes();
  }
}