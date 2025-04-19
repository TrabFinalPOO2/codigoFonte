package com.pinboard.demo.pattern.strategy;

import java.util.List;

/**
 * Interface para o padrão Strategy de ordenação de pins
 */
public interface SortingStrategy {
  <T> List<T> sort(List<T> items);
  String getDescription();
}