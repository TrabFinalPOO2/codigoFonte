package com.pinboard.demo.pattern.decorator;

/**
 * Componente concreto para o padrão Decorator
 */
public class BasicPin implements PinComponent {
  private String title;
  private String imageUrl;
  private int likes;

  public BasicPin(String title, String imageUrl, int likes) {
    this.title = title;
    this.imageUrl = imageUrl;
    this.likes = likes;
  }

  @Override
  public String render() {
    return "<div class='pin'>" +
           "<img src='" + imageUrl + "' alt='" + title + "'>" +
           "<h3>" + title + "</h3>" +
           "<span>" + likes + " likes</span>" +
           "</div>";
  }

  @Override
  public int getLikes() {
    return likes;
  }
}