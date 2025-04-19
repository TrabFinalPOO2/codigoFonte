package com.pinboard.demo.pattern.factory;

import java.util.List;
import com.pinboard.demo.model.Board;
import com.pinboard.demo.model.User;

/**
 * Interface de produto para o padrão Factory para layouts de boards
 */
public interface BoardLayout {
  String render();
  String renderWithBoards(List<Board> boards);
  String renderWithBoards(List<Board> boards, User currentUser);
  String getName();
}