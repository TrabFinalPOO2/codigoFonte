package com.pinboard.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pinboard.demo.model.Board;
import com.pinboard.demo.model.Pin;
import com.pinboard.demo.model.User;
import com.pinboard.demo.pattern.factory.LayoutFactory;
import com.pinboard.demo.pattern.factory.PinLayout;
import com.pinboard.demo.pattern.factory.BoardLayout;
import com.pinboard.demo.pattern.factory.BoardLayoutFactory;
import com.pinboard.demo.pattern.factory.BoardGridLayout;
import com.pinboard.demo.pattern.factory.BoardListLayout;
import com.pinboard.demo.pattern.factory.BoardMasonryLayout;
import com.pinboard.demo.pattern.observer.PinNotificationService;
import com.pinboard.demo.repository.BoardRepository;
import com.pinboard.demo.repository.PinRepository;

@Service
public class BoardService {

  @Autowired
  private BoardRepository boardRepository;
  
  @Autowired
  private PinRepository pinRepository;
  
  @Autowired
  private PinNotificationService notificationService;
  
  public List<Board> getAllBoards() {
    return boardRepository.findAll();
  }
  
  public List<Board> getPublicBoards() {
    return boardRepository.findByIsPrivateFalse();
  }
  
  public Board getBoard(Long id) {
    return boardRepository.findById(id).orElse(null);
  }
  
  public Board createBoard(Board board, User owner) {
    board.setOwner(owner);
    board.setCreatedAt(LocalDateTime.now());
    return boardRepository.save(board);
  }
  
  public Board updateBoard(Board board) {
    Board existingBoard = boardRepository.findById(board.getId()).orElse(null);
    if (existingBoard != null) {
      existingBoard.setName(board.getName());
      existingBoard.setDescription(board.getDescription());
      existingBoard.setPrivate(board.isPrivate());
      existingBoard.setCoverImageUrl(board.getCoverImageUrl());
      return boardRepository.save(existingBoard);
    }
    return null;
  }
  
  public void deleteBoard(Long id) {
    boardRepository.deleteById(id);
  }
  
  public Board addPinToBoard(Long boardId, Long pinId) {
    Board board = boardRepository.findById(boardId).orElse(null);
    Pin pin = pinRepository.findById(pinId).orElse(null);
    
    if (board != null && pin != null) {
      board.addPin(pin);
      boardRepository.save(board);
        
      // Notifica os observadores que um pin foi adicionado ao board
      notificationService.notifyObservers("Pin adicionado ao board: " + board.getName(), pin.getId());
          
      return board;
    }
    return null;
  }
  
  public Board removePinFromBoard(Long boardId, Long pinId) {
    Board board = boardRepository.findById(boardId).orElse(null);
    Pin pin = pinRepository.findById(pinId).orElse(null);
      
    if (board != null && pin != null) {
      board.removePin(pin);
      boardRepository.save(board);
      return board;
    }
    return null;
  }
  
  public List<Board> findBoardsByUser(User user) {
    return boardRepository.findByOwner(user);
  }
  
  public List<Board> searchBoards(String keyword) {
    return boardRepository.findByNameContainingIgnoreCase(keyword);
  }
  
  // Método que utiliza o padrão Factory para criar um layout para exibição de pins
  public String renderBoardLayout(String layoutType, Board board) {
    PinLayout layout = LayoutFactory.createLayout(layoutType);
    return layout.renderWithPins(board.getPins(), board.getId());
  }
  
  // Método original para retrocompatibilidade
  public String renderBoardLayout(String layoutType) {
    PinLayout layout = LayoutFactory.createLayout(layoutType);
    return layout.render();
  }
  
  // Método que utiliza o padrão Factory para criar um layout para a listagem de boards
  public String renderBoardsListLayout(String layoutType, List<Board> boards) {
    BoardLayout layout = BoardLayoutFactory.createLayout(layoutType);
    return layout.renderWithBoards(boards);
  }
  
  // Método que passa o usuário atual para verificar permissões de edição
  public String renderBoardsListLayout(String layoutType, List<Board> boards, User currentUser) {
    BoardLayout layout = BoardLayoutFactory.createLayout(layoutType);
    return ((layout instanceof BoardGridLayout) ? ((BoardGridLayout)layout).renderWithBoards(boards, currentUser) :
            (layout instanceof BoardMasonryLayout) ? ((BoardMasonryLayout)layout).renderWithBoards(boards, currentUser) :
            (layout instanceof BoardListLayout) ? ((BoardListLayout)layout).renderWithBoards(boards, currentUser) :
            layout.renderWithBoards(boards));
  }
}