package com.pinboard.demo.pattern.factory;

import java.util.Set;
import com.pinboard.demo.model.Pin;

/**
 * Produto concreto para o padrão Factory - Layout em lista
 */
public class ListLayout implements PinLayout {

  @Override
  public String render() {
    return "<div class='container'>" +
           "<p class='text-center text-muted mb-4'><i class='fa fa-list me-1'></i> Visualizando em formato Lista</p>" +
           "<div class='row'>" +
           "<div class='pin-list'>" +
           "<!-- Aqui serão inseridos os pins em formato de lista -->" +
           "</div>" +
           "</div>" +
           "</div>";
  }

  @Override
  public String renderWithPins(Set<Pin> pins, Long boardId) {
    StringBuilder html = new StringBuilder();
    html.append("<div class='container'>");
    html.append("<p class='text-center text-muted mb-4'><i class='fa fa-list me-1'></i> Visualizando em formato Lista</p>");
    html.append("<div class='pin-list'>");
    html.append("<div class='list-group'>");

    for (Pin pin : pins) {
      // Transformando todo o item em um link
      html.append("<a href='/pins/" + pin.getId() + "' style='text-decoration: none; color: inherit;' class='list-group-item list-group-item-action'>");
      html.append("<div class='d-flex w-100'>");

      // Imagem thumbnail
      html.append("<div class='pin-list-image me-3' style='width: 150px;'>");
      html.append("<img src='" + pin.getImageUrl() + "' class='img-fluid rounded' alt='" + pin.getTitle() + "'>");
      html.append("</div>");

      // Conteúdo
      html.append("<div class='flex-grow-1'>");

      // Cabeçalho com título e likes
      html.append("<div class='d-flex w-100 justify-content-between'>");
      html.append("<h5 class='mb-1'>" + pin.getTitle() + "</h5>");

      // Badge de destaque, se aplicável
      if (pin.isFeatured()) html.append("<span class='badge bg-warning'><i class='fa fa-star'></i> Destaque</span>");
      else html.append("<small class='text-muted'><i class='fa fa-heart'></i> " + pin.getLikes() + "</small>");

      html.append("</div>");

      // Descrição
      html.append("<p class='mb-1'>" + pin.getDescription() + "</p>");

      // Botões de ação - removido o botão "Ver"
      html.append("<div class='btn-group mt-2'>");
      html.append("<a href='/pins/" + pin.getId()
          + "/like' class='btn btn-sm btn-outline-danger' onclick='event.stopPropagation();'><i class='fa fa-heart'></i></a>");
      html.append("<a href='/boards/" + boardId + "/remove-pin/" + pin.getId()
          + "' class='btn btn-sm btn-outline-secondary' onclick='event.stopPropagation();'><i class='fa fa-times'></i></a>");
      html.append("</div>");

      html.append("</div>");
      html.append("</div>");
      html.append("</a>");
    }

    html.append("</div>");
    html.append("</div>");
    html.append("</div>");

    return html.toString();
  }

  @Override
  public String getName() {
    return "Lista";
  }

  @Override
  public int getColumnsCount() {
    return 1;
  }
}