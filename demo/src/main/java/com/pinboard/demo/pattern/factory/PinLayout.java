package com.pinboard.demo.pattern.factory;

import java.util.List;
import java.util.Set;
import com.pinboard.demo.model.Pin;

/**
 * Interface de produto para o padrão Factory
 */
public interface PinLayout {
  String render();
  String renderWithPins(Set<Pin> pins, Long boardId);
  String getName();
  int getColumnsCount();
}