package com.pinboard.demo.pattern.factory;

import java.util.List;
import com.pinboard.demo.model.Board;
import com.pinboard.demo.model.User;

/**
 * Layout em lista para exibição de boards
 */
public class BoardListLayout implements BoardLayout {

  @Override
  public String render() {
    return "<p class='text-center text-muted mb-4'><i class='fa fa-list me-1'></i> Visualizando boards em formato lista</p>";
  }

  @Override
  public String renderWithBoards(List<Board> boards) {
    // Chama método com usuário null (não verifica permissões de edição)
    return renderWithBoards(boards, null);
  }

  @Override
  public String renderWithBoards(List<Board> boards, User currentUser) {
    StringBuilder html = new StringBuilder();
    html.append("<p class='text-center text-muted mb-4'><i class='fa fa-list me-1'></i> Visualizando boards em formato lista</p>");
    html.append("<div class='board-list'>");
    html.append("<div class='list-group'>");

    for (Board board : boards) {
      html.append("<div class='list-group-item'>");
      html.append("<div class='d-flex'>");

      // Imagem de capa (thumbnail)
      html.append("<div class='board-list-image me-3' style='width: 180px;'>");
      html.append("<img src='").append(board.getCoverImageUrl() != null && !board.getCoverImageUrl().isEmpty()
          ? board.getCoverImageUrl()
          : "https://via.placeholder.com/180x120")
          .append("' class='img-fluid rounded' style='height: 120px; object-fit: cover;' alt='Board Cover'>");
      html.append("</div>");

      // Conteúdo
      html.append("<div class='flex-grow-1'>");
      html.append("<div class='d-flex justify-content-between align-items-center'>");
      html.append("<h5 class='mb-1'>").append(board.getName()).append("</h5>");

      // Pins count
      html.append("<small class='text-muted'><i class='fa fa-thumbtack'></i> ")
          .append(board.getPins().size())
          .append(" pins</small>");
      html.append("</div>");

      // Descrição
      html.append("<p class='mb-1'>").append(board.getDescription()).append("</p>");

      // Informações do proprietário
      html.append("<div class='d-flex mt-2 align-items-center'>");
      if (board.getOwner() != null && board.getOwner().getProfileImageUrl() != null && !board.getOwner().getProfileImageUrl().isEmpty()) {
        html.append("<img src='").append(board.getOwner().getProfileImageUrl())
            .append("' class='rounded-circle me-1' style='width: 24px; height: 24px; object-fit: cover;' alt='Avatar'>");
      } else html.append("<img src='/img/default-avatar.png' class='rounded-circle me-1' style='width: 24px; height: 24px; object-fit: cover;' alt='Avatar padrão'>");

      html.append("<small class='text-muted'>")
          .append(board.getOwner() != null ? board.getOwner().getUsername() : "Anônimo")
          .append("</small>");

      // Botões de ação
      html.append("<div class='ms-auto'>");
      html.append("<a href='/boards/").append(board.getId()).append("' class='btn btn-sm btn-primary me-2'>");
      html.append("<i class='fa fa-eye'></i> Ver Board</a>");

      // Verifica se o usuário atual é o dono do board antes de mostrar o botão de edição
      boolean isOwner = currentUser != null && board.getOwner() != null &&
          currentUser.getId().equals(board.getOwner().getId());

      if (isOwner) {
        html.append("<a href='/boards/").append(board.getId())
            .append("/edit' class='btn btn-sm btn-outline-secondary'>");
        html.append("<i class='fa fa-edit'></i> Editar</a>");
      }

      html.append("</div>");
      html.append("</div>");

      html.append("</div>");
      html.append("</div>");
      html.append("</div>");
    }

    html.append("</div>");
    html.append("</div>");

    return html.toString();
  }

  @Override
  public String getName() {
    return "Lista";
  }
}