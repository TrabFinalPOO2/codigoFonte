package com.pinboard.demo.pattern.decorator;

/**
 * Decorador concreto que adiciona funcionalidade de destaque ao Pin
 */
public class FeaturedPinDecorator extends PinDecorator {

  public FeaturedPinDecorator(PinComponent decoratedPin) {
    super(decoratedPin);
  }

  @Override
  public String render() {
    return "<div class='pin featured'>" +
           "<div class='featured-badge'><i class='fa fa-star'></i> Destaque</div>" +
           decoratedPin.render().replace("<div class='pin'>", "");
  }
}