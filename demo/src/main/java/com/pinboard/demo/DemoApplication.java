package com.pinboard.demo;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.pinboard.demo.model.Board;
import com.pinboard.demo.model.Pin;
import com.pinboard.demo.model.User;
import com.pinboard.demo.pattern.observer.ActivityLogObserver;
import com.pinboard.demo.pattern.observer.PinNotificationService;
import com.pinboard.demo.pattern.singleton.ConfigurationManager;
import com.pinboard.demo.repository.BoardRepository;
import com.pinboard.demo.repository.PinRepository;
import com.pinboard.demo.repository.UserRepository;

@SpringBootApplication
public class DemoApplication {

  public static void main(String[] args) {
    // Configura o Singleton antes de iniciar a aplicação
    ConfigurationManager configManager = ConfigurationManager.getInstance();
    configManager.setAppName("Pinboard");
    configManager.setVersion("1.0.0");
    configManager.setDebugMode(true);
    
    SpringApplication.run(DemoApplication.class, args);
  }
  
  @Bean
  public PinNotificationService pinNotificationService() {
    return new PinNotificationService();
  }
  
  @Bean
  public CommandLineRunner dataLoader(UserRepository userRepository, 
                                      PinRepository pinRepository, 
                                      BoardRepository boardRepository,
                                      PinNotificationService notificationService,
                                      ActivityLogObserver activityLogObserver) {
    return args -> {
      // Registra o observer para o padrão Observer
      notificationService.registerObserver(activityLogObserver);
      
      // Verifica se o banco de dados está vazio antes de criar dados de demonstração
      if (userRepository.count() > 0) {
        System.out.println("Banco de dados já contém dados. Pulando criação de dados de demonstração.");
        return; // Não cria dados de demonstração se já existirem registros
      }
      
      System.out.println("Inicializando banco de dados com dados de demonstração...");
      
      // Criar alguns usuários de demonstração
      User user1 = new User();
      user1.setUsername("maria_silva");
      user1.setEmail("maria@example.com");
      user1.setPassword("senha123");
      user1.setProfileImageUrl("https://randomuser.me/api/portraits/women/44.jpg");
      user1.setBio("Entusiasta de arte e design");
      
      User user2 = new User();
      user2.setUsername("joao_santos");
      user2.setEmail("joao@example.com");
      user2.setPassword("senha123");
      user2.setProfileImageUrl("https://randomuser.me/api/portraits/men/22.jpg");
      user2.setBio("Fotógrafo amador e amante de viagens");
      
      // Salva os usuários
      userRepository.saveAll(Arrays.asList(user1, user2));
      
      // Cria alguns boards
      Board board1 = new Board();
      board1.setName("Inspirações de Design");
      board1.setDescription("Ideias criativas para projetos de design gráfico");
      board1.setCreatedAt(LocalDateTime.now().minusDays(5));
      board1.setCoverImageUrl("https://images.unsplash.com/photo-1558655146-d09347e92766");
      board1.setPrivate(false);
      board1.setOwner(user1);
      
      Board board2 = new Board();
      board2.setName("Destinos de Viagem");
      board2.setDescription("Lugares incríveis para conhecer");
      board2.setCreatedAt(LocalDateTime.now().minusDays(2));
      board2.setCoverImageUrl("https://images.unsplash.com/photo-1503220317375-aaad61436b1b");
      board2.setPrivate(false);
      board2.setOwner(user2);
      
      // Salva os boards
      boardRepository.saveAll(Arrays.asList(board1, board2));
      
      // Cria alguns pins
      Pin pin1 = new Pin();
      pin1.setTitle("Design Minimalista");
      pin1.setDescription("Exemplo de design minimalista para interfaces modernas");
      pin1.setImageUrl("https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85");
      pin1.setLikes(42);
      pin1.setCreatedAt(LocalDateTime.now().minusDays(4));
      pin1.setCreator(user1);
      pin1.setFeatured(true);
      
      Pin pin2 = new Pin();
      pin2.setTitle("Tipografia Criativa");
      pin2.setDescription("Exemplos de tipografia para inspirar seus projetos");
      pin2.setImageUrl("https://images.unsplash.com/photo-1517998873486-b1d89e52b374");
      pin2.setLikes(27);
      pin2.setCreatedAt(LocalDateTime.now().minusDays(3));
      pin2.setCreator(user1);
      pin2.setFeatured(false);
      
      Pin pin3 = new Pin();
      pin3.setTitle("Santorini, Grécia");
      pin3.setDescription("Vista deslumbrante das ilhas gregas");
      pin3.setImageUrl("https://images.unsplash.com/photo-1469796466635-455ede028aca");
      pin3.setLikes(89);
      pin3.setCreatedAt(LocalDateTime.now().minusDays(2));
      pin3.setCreator(user2);
      pin3.setFeatured(true);
      
      Pin pin4 = new Pin();
      pin4.setTitle("Montanhas do Japão");
      pin4.setDescription("Paisagem natural das montanhas de Hakone");
      pin4.setImageUrl("https://images.unsplash.com/photo-1528164344705-47542687000d");
      pin4.setLikes(54);
      pin4.setCreatedAt(LocalDateTime.now().minusDays(1));
      pin4.setCreator(user2);
      pin4.setFeatured(false);
      
      // Salva os pins
      pinRepository.saveAll(Arrays.asList(pin1, pin2, pin3, pin4));
      
      // Adiciona pins aos boards
      board1.addPin(pin1);
      board1.addPin(pin2);
      board2.addPin(pin3);
      board2.addPin(pin4);
      
      // Atualiza boards
      boardRepository.saveAll(Arrays.asList(board1, board2));
      
      // Notifica sobre a criação dos pins
      notificationService.notifyObservers("Aplicação inicializada com dados de demonstração", 0L);
      
      System.out.println("Banco de dados inicializado com sucesso!");
    };
  }
}
