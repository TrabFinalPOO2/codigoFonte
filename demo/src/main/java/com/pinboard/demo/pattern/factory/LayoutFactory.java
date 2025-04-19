package com.pinboard.demo.pattern.factory;

/**
 * Factory concreta para o padrão Factory
 */
public class LayoutFactory {

  public static PinLayout createLayout(String type) {
    switch (type.toLowerCase()) {
      case "grid": return new GridLayout();
      case "list": return new ListLayout();
      case "masonry": return new MasonryLayout();
      default: return new GridLayout(); // Layout padrão
    }
  }
}