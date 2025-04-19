package com.pinboard.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pinboard.demo.pattern.singleton.ConfigurationManager;

/**
 * Controller para a página "About" que exibe informações sobre a aplicação
 * e os padrões de projeto implementados
 */
@Controller
@RequestMapping("/about")
public class AboutController {

  // Obtém a instância do Singleton para configurações
  private final ConfigurationManager configManager = ConfigurationManager.getInstance();

  @GetMapping
  public String about(Model model) {
    // Adiciona informações da aplicação ao modelo
    model.addAttribute("appName", configManager.getAppName());
    model.addAttribute("appVersion", configManager.getVersion());

    // Adiciona informações sobre os padrões de projeto
    model.addAttribute("patternsSummary", getPatternsSummary());

    return "about";
  }

  private String getPatternsSummary() {
    return "Aplicação desenvolvida com Spring Boot utilizando diversos padrões de projeto " +
        "para demonstrar boas práticas de desenvolvimento e arquitetura de software.";
  }
}