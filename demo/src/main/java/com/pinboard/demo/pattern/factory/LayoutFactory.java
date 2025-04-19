package com.pinboard.demo.pattern.factory;

/**
 * Factory concreta para o padrão Factory
 */
public class LayoutFactory {

  public static PinLayout createLayout(String type) {
    return switch (type.toLowerCase()) {
      case "grid" -> new GridLayout();
      case "list" -> new ListLayout();
      case "masonry" -> new MasonryLayout();
      default -> new GridLayout(); // Layout padrão
    };
  }
}