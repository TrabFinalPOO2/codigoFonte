package com.pinboard.demo.pattern.observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Implementação concreta do Observer para o padrão Observer
 */
@Component
public class ActivityLogObserver implements PinObserver {
  private List<String> activityLog = new ArrayList<>();

  @Override
  public void update(String message, Long pinId) {
    String logEntry = LocalDateTime.now() + " - Pin #" + pinId + ": " + message;
    activityLog.add(logEntry);
    System.out.println("Log registrado: " + logEntry);
  }

  public List<String> getRecentActivities() {
    // Retorna as últimas 10 atividades ou menos se não houver 10
    int size = activityLog.size();
    return activityLog.subList(Math.max(0, size - 10), size);
  }
}