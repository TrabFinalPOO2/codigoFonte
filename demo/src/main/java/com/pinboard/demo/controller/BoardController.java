package com.pinboard.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pinboard.demo.model.Board;
import com.pinboard.demo.model.User;
import com.pinboard.demo.pattern.singleton.ConfigurationManager;
import com.pinboard.demo.service.BoardService;
import com.pinboard.demo.service.PinService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/boards")
public class BoardController {

    @Autowired
    private BoardService boardService;
    
    @Autowired
    private PinService pinService;
    
    // Obtém a instância do Singleton para configurações
    private final ConfigurationManager configManager = ConfigurationManager.getInstance();
    
    @GetMapping
    public String getAllBoards(Model model) {
        List<Board> boards = boardService.getPublicBoards();
        model.addAttribute("boards", boards);
        model.addAttribute("appName", configManager.getAppName());
        return "boards/index";
    }
    
    @GetMapping("/{id}")
    public String getBoard(@PathVariable Long id, @RequestParam(required = false) String layout, Model model, HttpSession session) {
        Board board = boardService.getBoard(id);
        if (board == null) {
            return "redirect:/boards";
        }
        
        // Verificar se o board é privado e o usuário tem acesso
        User currentUser = (User) session.getAttribute("currentUser");
        if (board.isPrivate() && (currentUser == null || !board.getOwner().getId().equals(currentUser.getId()))) {
            return "redirect:/boards";
        }
        
        // Adiciona informação se o usuário é o proprietário do board
        boolean isOwner = currentUser != null && board.getOwner() != null && 
                        board.getOwner().getId().equals(currentUser.getId());
        model.addAttribute("isOwner", isOwner);
        
        // Utiliza o padrão Factory para criar o layout adequado
        String layoutType = layout != null ? layout : "masonry";
        String boardLayout = boardService.renderBoardLayout(layoutType);
        
        model.addAttribute("board", board);
        model.addAttribute("boardLayoutHtml", boardLayout);
        model.addAttribute("pins", board.getPins());
        model.addAttribute("appName", configManager.getAppName());
        
        return "boards/show";
    }
    
    @GetMapping("/new")
    public String newBoardForm(Model model, HttpSession session) {
        // Verificar se o usuário está logado
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("board", new Board());
        model.addAttribute("appName", configManager.getAppName());
        return "boards/new";
    }
    
    @PostMapping
    public String createBoard(@ModelAttribute Board board, @RequestParam(required = false) Boolean private_,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        // Configura a propriedade private com base no parâmetro recebido
        board.setPrivate(private_ != null && private_);
        
        // Obtém o usuário da sessão
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            // Em caso de usuário não autenticado, redirecionamos para login
            return "redirect:/auth/login";
        }
        
        boardService.createBoard(board, currentUser);
        redirectAttributes.addFlashAttribute("success", "Board criado com sucesso!");
        return "redirect:/boards";
    }
    
    @GetMapping("/{id}/edit")
    public String editBoardForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Verificar se o usuário está logado
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        Board board = boardService.getBoard(id);
        if (board == null) {
            return "redirect:/boards";
        }
        
        // Verificar se o usuário é o proprietário do board
        if (board.getOwner() == null || !board.getOwner().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Você não tem permissão para editar este board");
            return "redirect:/boards/" + id;
        }
        
        model.addAttribute("board", board);
        model.addAttribute("appName", configManager.getAppName());
        return "boards/edit";
    }
    
    @PostMapping("/{id}")
    @Transactional
    public String updateBoard(@PathVariable Long id, @ModelAttribute Board board, @RequestParam(required = false) Boolean private_,
                            HttpSession session, RedirectAttributes redirectAttributes) {
        // Verificar se o usuário está logado
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        Board existingBoard = boardService.getBoard(id);
        if (existingBoard == null) {
            return "redirect:/boards";
        }
        
        // Verificar se o usuário é o proprietário do board
        if (existingBoard.getOwner() == null || !existingBoard.getOwner().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Você não tem permissão para editar este board");
            return "redirect:/boards/" + id;
        }
        
        // Atualiza as propriedades do board
        board.setId(id);
        board.setOwner(existingBoard.getOwner());
        board.setCreatedAt(existingBoard.getCreatedAt());
        board.setPins(existingBoard.getPins());
        board.setPrivate(private_ != null && private_);
        
        boardService.updateBoard(board);
        redirectAttributes.addFlashAttribute("success", "Board atualizado com sucesso!");
        return "redirect:/boards/" + id;
    }
    
    @GetMapping("/{id}/delete")
    public String deleteBoard(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        // Verificar se o usuário está logado
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        Board board = boardService.getBoard(id);
        if (board == null) {
            return "redirect:/boards";
        }
        
        // Verificar se o usuário é o proprietário do board
        if (board.getOwner() == null || !board.getOwner().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Você não tem permissão para excluir este board");
            return "redirect:/boards/" + id;
        }
        
        boardService.deleteBoard(id);
        redirectAttributes.addFlashAttribute("success", "Board excluído com sucesso!");
        return "redirect:/boards";
    }
    
    @GetMapping("/{boardId}/add-pin/{pinId}")
    public String addPinToBoard(@PathVariable Long boardId, @PathVariable Long pinId, 
                               HttpSession session, RedirectAttributes redirectAttributes) {
        // Verificar se o usuário está logado
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        Board board = boardService.getBoard(boardId);
        if (board == null) {
            return "redirect:/boards";
        }
        
        // Verificar se o usuário é o proprietário do board
        if (board.getOwner() == null || !board.getOwner().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Você não tem permissão para modificar este board");
            return "redirect:/boards/" + boardId;
        }
        
        boardService.addPinToBoard(boardId, pinId);
        return "redirect:/boards/" + boardId;
    }
    
    @GetMapping("/{boardId}/remove-pin/{pinId}")
    public String removePinFromBoard(@PathVariable Long boardId, @PathVariable Long pinId,
                                    HttpSession session, RedirectAttributes redirectAttributes) {
        // Verificar se o usuário está logado
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        Board board = boardService.getBoard(boardId);
        if (board == null) {
            return "redirect:/boards";
        }
        
        // Verificar se o usuário é o proprietário do board
        if (board.getOwner() == null || !board.getOwner().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Você não tem permissão para modificar este board");
            return "redirect:/boards/" + boardId;
        }
        
        boardService.removePinFromBoard(boardId, pinId);
        return "redirect:/boards/" + boardId;
    }
    
    @GetMapping("/search")
    public String searchBoards(@RequestParam String keyword, Model model) {
        List<Board> boards = boardService.searchBoards(keyword);
        model.addAttribute("boards", boards);
        model.addAttribute("keyword", keyword);
        model.addAttribute("appName", configManager.getAppName());
        return "boards/search";
    }
}