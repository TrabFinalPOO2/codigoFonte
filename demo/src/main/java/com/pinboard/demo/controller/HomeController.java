package com.pinboard.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.pinboard.demo.pattern.observer.ActivityLogObserver;
import com.pinboard.demo.pattern.singleton.ConfigurationManager;
import com.pinboard.demo.service.PinService;

@Controller
public class HomeController {

  @Autowired
  private PinService pinService;

  @Autowired
  private ActivityLogObserver activityLogObserver;

  // Obtém a instância do Singleton para configurações
  private final ConfigurationManager configManager = ConfigurationManager.getInstance();

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("featuredPins", pinService.findFeaturedPins());
    model.addAttribute("allPins", pinService.getAllPins());
    model.addAttribute("appName", configManager.getAppName());
    model.addAttribute("version", configManager.getVersion());
    return "home";
  }

  @GetMapping("/activities")
  public String activities(Model model) {
    model.addAttribute("activities", activityLogObserver.getRecentActivities());
    model.addAttribute("appName", configManager.getAppName());
    return "activities";
  }
}