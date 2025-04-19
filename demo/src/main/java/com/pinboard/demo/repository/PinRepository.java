package com.pinboard.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pinboard.demo.model.Pin;
import com.pinboard.demo.model.User;

@Repository
public interface PinRepository extends JpaRepository<Pin, Long> {
  List<Pin> findByCreator(User creator);
  List<Pin> findByFeaturedTrue();
  List<Pin> findByTitleContainingIgnoreCase(String keyword);
}