package com.pinboard.demo.pattern.factory;

import java.util.Set;
import com.pinboard.demo.model.Pin;

/**
 * Produto concreto para o padrão Factory - Layout em cascata/masonry
 */
public class MasonryLayout implements PinLayout {

  @Override
  public String render() {
    return "<div class='container'>" +
           "<p class='text-center text-muted mb-4'><i class='fa fa-th-large me-1'></i> Visualizando em formato Cascata</p>" +
           "<div class='row'>" +
           "<div class='masonry-grid' data-masonry='{\"itemSelector\": \".masonry-item\", \"percentPosition\": true, \"gutter\": 10}'>" +
           "<!-- Aqui serão inseridos os pins em formato masonry -->" +
           "</div>" +
           "</div>" +
           "</div>";
  }

  @Override
  public String renderWithPins(Set<Pin> pins, Long boardId) {
    StringBuilder html = new StringBuilder();
    html.append("<div class='container'>");
    html.append("<p class='text-center text-muted mb-4'><i class='fa fa-th-large me-1'></i> Visualizando em formato Cascata</p>");
    html.append("<div class='pins-masonry'>");
    html.append("<div class='masonry-grid' style='position: relative;'>");

    for (Pin pin : pins) {
      html.append("<div class='masonry-item' style='width: 23%; margin: 1%; float: left;'>");
      // Envolvendo todo o card com um link
      html.append("<a href='/pins/" + pin.getId() + "' style='text-decoration: none; color: inherit;'>");
      html.append("<div class='card pin-card h-100'>");

      // Badge de destaque, se aplicável
      if (pin.isFeatured()) html.append("<div class='featured-badge'><i class='fa fa-star'></i> Destaque</div>");

      // Imagem
      html.append("<img src='" + pin.getImageUrl() + "' class='card-img-top' alt='" + pin.getTitle() + "'>");

      // Corpo do card
      html.append("<div class='card-body'>");
      html.append("<h5 class='card-title'>" + pin.getTitle() + "</h5>");
      html.append("<p class='card-text text-truncate'>" + pin.getDescription() + "</p>");
      html.append("</div>");

      // Footer
      html.append("<div class='card-footer d-flex justify-content-between align-items-center'>");
      html.append("<small class='text-muted'><i class='fa fa-heart'></i> " + pin.getLikes() + " likes</small>");

      // Botões de ação - removido o botão "Ver"
      html.append("<div class='btn-group'>");
      html.append("<a href='/pins/" + pin.getId()
          + "/like' class='btn btn-sm btn-outline-danger' onclick='event.stopPropagation();'><i class='fa fa-heart'></i></a>");
      html.append("<a href='/boards/" + boardId + "/remove-pin/" + pin.getId()
          + "' class='btn btn-sm btn-outline-secondary' onclick='event.stopPropagation();'><i class='fa fa-times'></i></a>");
      html.append("</div>");

      html.append("</div>");
      html.append("</div>");
      html.append("</a>");
      html.append("</div>");
    }

    html.append("</div>");
    html.append("</div>");
    html.append("</div>");

    return html.toString();
  }

  @Override
  public String getName() {
    return "Cascata";
  }

  @Override
  public int getColumnsCount() {
    return 0; // Auto-ajustável
  }
}