package com.pinboard.demo.pattern.factory;

/**
 * Factory para criar diferentes layouts de exibição de boards
 */
public class BoardLayoutFactory {

  public static BoardLayout createLayout(String type) {
    switch (type.toLowerCase()) {
      case "grid": return new BoardGridLayout();
      case "list": return new BoardListLayout();
      case "masonry": return new BoardMasonryLayout();
      default: return new BoardGridLayout(); // Layout padrão
    }
  }
}