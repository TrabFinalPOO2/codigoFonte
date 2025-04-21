package com.pinboard.demo.pattern.factory;

import java.util.Set;
import com.pinboard.demo.model.Pin;

/**
 * Produto concreto para o padrão Factory - Layout em grade
 */
public class GridLayout implements PinLayout {

  @Override
  public String render() {
    return "<div class='container'>" +
           "<p class='text-center text-muted mb-4'><i class='fa fa-th me-1'></i> Visualizando em formato Grade</p>" +
           "<div class='row'>" +
           "<div class='pin-grid'>" +
           "<!-- Os pins serão inseridos dinamicamente -->" +
           "</div>" +
           "</div>" +
           "</div>";
  }

  @Override
  public String renderWithPins(Set<Pin> pins, Long boardId) {
    StringBuilder html = new StringBuilder();
    html.append("<div class='container'>");
    html.append("<p class='text-center text-muted mb-4'><i class='fa fa-th me-1'></i> Visualizando em formato Grade</p>");
    html.append("<div class='row g-4'>");

    for (Pin pin : pins) {
      html.append("<div class='col-sm-6 col-md-4 col-lg-3 pin-item'>");
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

    return html.toString();
  }

  @Override
  public String getName() {
    return "Grade";
  }

  @Override
  public int getColumnsCount() {
    return 4;
  }
}