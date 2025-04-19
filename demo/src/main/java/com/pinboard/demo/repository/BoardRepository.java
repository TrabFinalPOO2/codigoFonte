package com.pinboard.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pinboard.demo.model.Board;
import com.pinboard.demo.model.User;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
  List<Board> findByOwner(User owner);
  List<Board> findByIsPrivateFalse();
  List<Board> findByNameContainingIgnoreCase(String keyword);
}