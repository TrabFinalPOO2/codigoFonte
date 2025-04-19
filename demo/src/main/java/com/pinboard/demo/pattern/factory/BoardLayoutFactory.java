package com.pinboard.demo.pattern.factory;

/**
 * Factory para criar diferentes layouts de exibição de boards
 */
public class BoardLayoutFactory {

  public static BoardLayout createLayout(String type) {
    return switch (type.toLowerCase()) {
      case "grid" -> new BoardGridLayout();
      case "list" -> new BoardListLayout();
      case "masonry" -> new BoardMasonryLayout();
      default -> new BoardGridLayout(); // Layout padrão
    };
  }
}