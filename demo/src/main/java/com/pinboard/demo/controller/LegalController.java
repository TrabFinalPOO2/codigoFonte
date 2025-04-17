package com.pinboard.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pinboard.demo.pattern.singleton.ConfigurationManager;

/**
 * Controller para as páginas de Termos de Uso e Política de Privacidade
 */
@Controller
@RequestMapping("/legal")
public class LegalController {
    
    // Obtém a instância do Singleton para configurações
    private final ConfigurationManager configManager = ConfigurationManager.getInstance();
    
    @GetMapping("/terms")
    public String termsOfUse(Model model) {
        // Adiciona informações da aplicação ao modelo
        model.addAttribute("appName", configManager.getAppName());
        model.addAttribute("appVersion", configManager.getVersion());
        return "legal/terms";
    }
    
    @GetMapping("/privacy")
    public String privacyPolicy(Model model) {
        // Adiciona informações da aplicação ao modelo
        model.addAttribute("appName", configManager.getAppName());
        model.addAttribute("appVersion", configManager.getVersion());
        return "legal/privacy";
    }
}