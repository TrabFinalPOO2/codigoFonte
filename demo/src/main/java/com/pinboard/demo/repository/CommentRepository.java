package com.pinboard.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pinboard.demo.model.Comment;
import com.pinboard.demo.model.Pin;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPinOrderByCreatedAtDesc(Pin pin);
    List<Comment> findByPin_IdOrderByCreatedAtDesc(Long pinId);
    long countByPin_Id(Long pinId);
}