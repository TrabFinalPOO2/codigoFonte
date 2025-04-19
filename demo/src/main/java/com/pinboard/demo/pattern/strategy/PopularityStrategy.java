package com.pinboard.demo.pattern.strategy;

import java.util.List;
import java.util.stream.Collectors;

import com.pinboard.demo.model.Pin;

/**
 * Implementação concreta do Strategy para ordenar pins por popularidade
 */
public class PopularityStrategy implements SortingStrategy {

  @Override
  public <T> List<T> sort(List<T> items) {
    return items.stream()
        .sorted((o1, o2) -> {
          if (o1 instanceof Pin && o2 instanceof Pin) return ((Pin) o2).getLikes() - ((Pin) o1).getLikes();
          return 0;
        })
        .collect(Collectors.toList());
  }

  @Override
  public String getDescription() {
    return "Ordenar por Popularidade";
  }
}