package com.pinboard.demo.pattern.decorator;

/**
 * Decorador concreto que adiciona funcionalidade de compartilhamento ao Pin
 */
public class SocialSharingDecorator extends PinDecorator {

  public SocialSharingDecorator(PinComponent decoratedPin) {
    super(decoratedPin);
  }

  @Override
  public String render() {
    String original = decoratedPin.render();
    String withSharingButtons = original.replace("</div>",
            "<div class='social-buttons'>" +
            "<button class='btn btn-sm btn-primary'><i class='fa fa-facebook'></i></button>" +
            "<button class='btn btn-sm btn-info'><i class='fa fa-twitter'></i></button>" +
            "<button class='btn btn-sm btn-danger'><i class='fa fa-pinterest'></i></button>" +
            "</div></div>");

    return withSharingButtons;
  }
}