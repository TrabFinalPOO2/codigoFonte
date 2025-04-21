// Script principal para Pinboard

document.addEventListener('DOMContentLoaded', function () {
  // Inicialização de tooltips e popovers do Bootstrap
  var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
  var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
    return new bootstrap.Tooltip(tooltipTriggerEl);
  });

  // Implementação básica de efeito masonry para os pins
  // Em produção, usaríamos uma biblioteca como Masonry.js
  try {
    adjustMasonryLayout();
    window.addEventListener('resize', adjustMasonryLayout);
  } catch (e) {
    console.error('Erro ao configurar layout masonry:', e);
  }

  // Adiciona animação ao clicar em like
  document.querySelectorAll('.btn-outline-danger').forEach(function (btn) {
    btn.addEventListener('click', function (e) {
      if (this.getAttribute('href') && this.getAttribute('href').includes('/like')) {
        this.classList.add('animate__animated', 'animate__heartBeat');
        setTimeout(() => {
          this.classList.remove('animate__animated', 'animate__heartBeat');
        }, 1000);
      }
    });
  });

  // Comportamento minimalista para a barra de pesquisa
  const searchForm = document.querySelector('.search-form');
  if (searchForm) {
    const searchInput = searchForm.querySelector('.search-input');
    const searchButton = searchForm.querySelector('.search-button');

    // Pequeno efeito de foco
    searchInput.addEventListener('focus', function () {
      searchButton.style.color = '#e60023';
    });

    searchInput.addEventListener('blur', function () {
      if (this.value.trim() === '') searchButton.style.color = '#888';
    });
  }

  // Efeitos para a navbar
  const navbar = document.querySelector('.custom-navbar');

  // Detecta a página atual para destacar o item de menu correto
  if (navbar) {
    const navLinks = navbar.querySelectorAll('.nav-link-custom');
    const currentPath = window.location.pathname;

    navLinks.forEach(link => {
      const href = link.getAttribute('href');
      if (href === currentPath ||
        (href !== '/' && currentPath.startsWith(href))) {
        link.classList.add('active');
        link.style.color = '#e60023';
      }
    });
  }
});

// Função para ajustar o layout masonry
function adjustMasonryLayout() {
  const grids = document.querySelectorAll('.masonry-grid');

  grids.forEach(grid => {
    const items = grid.querySelectorAll('.masonry-item');
    const columnWidth = items[0] ? items[0].offsetWidth : 0;

    if (!columnWidth) return;

    // Calcula quantas colunas cabem na tela
    const containerWidth = grid.offsetWidth;
    const columnCount = Math.floor(containerWidth / columnWidth);

    if (columnCount <= 1) return; // Não precisa de masonry para uma coluna

    // Cria array para rastrear altura das colunas
    let columnHeights = new Array(columnCount).fill(0);

    // Posiciona cada item
    items.forEach((item, index) => {
      // Encontra a coluna mais curta
      const shortestColumnIndex = columnHeights.indexOf(Math.min(...columnHeights));

      // Posiciona o item
      const leftPosition = shortestColumnIndex * (100 / columnCount);
      item.style.position = 'absolute';
      item.style.width = (100 / columnCount) + '%';
      item.style.left = leftPosition + '%';
      item.style.top = columnHeights[shortestColumnIndex] + 'px';

      // Atualiza a altura da coluna
      columnHeights[shortestColumnIndex] += item.offsetHeight + 20; // 20px de margem
    });

    // Define a altura do container para o masonry
    grid.style.position = 'relative';
    grid.style.height = Math.max(...columnHeights) + 'px';
  });
}