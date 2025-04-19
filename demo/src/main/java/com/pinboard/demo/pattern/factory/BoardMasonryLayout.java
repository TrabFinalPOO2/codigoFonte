package com.pinboard.demo.pattern.factory;

import java.util.List;
import com.pinboard.demo.model.Board;
import com.pinboard.demo.model.User;

/**
 * Layout em cascata (masonry) para exibição de boards
 */
public class BoardMasonryLayout implements BoardLayout {

  @Override
  public String render() {
    return "<p class='text-center text-muted mb-4'><i class='fa fa-th-large me-1'></i> Visualizando boards em formato cascata</p>";
  }

  @Override
  public String renderWithBoards(List<Board> boards) {
    // Chama do método com usuário null (não verifica permissões de edição)
    return renderWithBoards(boards, null);
  }

  @Override
  public String renderWithBoards(List<Board> boards, User currentUser) {
    StringBuilder html = new StringBuilder();
    html.append(
        "<p class='text-center text-muted mb-4'><i class='fa fa-th-large me-1'></i> Visualizando boards em formato cascata</p>");
    html.append("<div class='boards-masonry'>");
    html.append("<div class='masonry-grid' style='position: relative;'>");

    for (Board board : boards) {
      html.append("<div class='masonry-item' style='width: 31%; margin: 1%; float: left;'>");
      html.append("<div class='card board-card h-100 shadow-sm'>");

      // Imagem de capa
      html.append("<img src='").append(board.getCoverImageUrl() != null && !board.getCoverImageUrl().isEmpty()
          ? board.getCoverImageUrl()
          : "https://via.placeholder.com/400x200")
          .append("' class='card-img-top' alt='Board Cover'>");

      // Corpo do card
      html.append("<div class='card-body'>");
      html.append("<h5 class='card-title'>").append(board.getName()).append("</h5>");
      html.append("<p class='card-text'>").append(board.getDescription()).append("</p>");

      // Informações do proprietário
      html.append("<div class='d-flex align-items-center mb-2'>");
      if (board.getOwner() != null && board.getOwner().getProfileImageUrl() != null && !board.getOwner().getProfileImageUrl().isEmpty()) {
        html.append("<img src='").append(board.getOwner().getProfileImageUrl())
            .append("' class='rounded-circle me-1' style='width: 24px; height: 24px; object-fit: cover;' alt='Avatar'>");
      } else html.append("<img src='/img/default-avatar.png' class='rounded-circle me-1' style='width: 24px; height: 24px; object-fit: cover;' alt='Avatar padrão'>");

      html.append("<small class='text-muted'>")
          .append(board.getOwner() != null ? board.getOwner().getUsername() : "Anônimo")
          .append("</small>");

      html.append("<small class='ms-auto text-muted'>");
      html.append("<i class='fa fa-thumbtack'></i> ")
          .append(board.getPins().size())
          .append(" pins</small>");
      html.append("</div>");
      html.append("</div>");

      // Footer
      html.append("<div class='card-footer'>");
      html.append("<a href='/boards/").append(board.getId()).append("' class='btn btn-sm btn-primary'>");
      html.append("<i class='fa fa-eye'></i> Ver Board</a>");

      // Verifica se o usuário atual é o dono do board antes de mostrar o botão de edição
      boolean isOwner = currentUser != null && board.getOwner() != null &&
          currentUser.getId().equals(board.getOwner().getId());

      if (isOwner) {
        html.append("<a href='/boards/").append(board.getId())
            .append("/edit' class='btn btn-sm btn-outline-secondary ms-2'>");
        html.append("<i class='fa fa-edit'></i> Editar</a>");
      }

      html.append("</div>");
      html.append("</div>");
      html.append("</div>");
    }

    html.append("</div>");
    html.append("</div>");
    html.append("<style>");
    html.append(".boards-masonry .card { margin-bottom: 20px; }");
    html.append("@media (max-width: 768px) { .masonry-item { width: 48% !important; } }");
    html.append("@media (max-width: 576px) { .masonry-item { width: 100% !important; } }");
    html.append("</style>");

    return html.toString();
  }

  @Override
  public String getName() {
    return "Cascata";
  }
}