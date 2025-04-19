package com.pinboard.demo.pattern.singleton;

/**
 * Implementação do padrão de projeto Singleton
 * Este padrão garante que uma classe tenha apenas uma instância e fornece um
 * ponto
 * global de acesso a ela.
 */
public class ConfigurationManager {
  private static ConfigurationManager instance;
  private String appName;
  private String version;
  private boolean debugMode;

  private ConfigurationManager() {
    // Configurações padrão
    this.appName = "Pinboard";
    this.version = "1.0.0";
    this.debugMode = false;
  }

  public static synchronized ConfigurationManager getInstance() {
    if (instance == null) {
      instance = new ConfigurationManager();
    }
    return instance;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public boolean isDebugMode() {
    return debugMode;
  }

  public void setDebugMode(boolean debugMode) {
    this.debugMode = debugMode;
  }
}