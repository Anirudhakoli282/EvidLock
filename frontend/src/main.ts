/// <reference types="@angular/localize" />

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';

import '@angular/localize/init';


platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));

// Aggiungi l'ascoltatore dell'evento di clic quando il documento è pronto
document.addEventListener('DOMContentLoaded', () => {
  const backToTopButton = document.getElementById('back-to-top');
  
  if (backToTopButton) {
    backToTopButton.addEventListener('click', () => {
      // Chiamata alla funzione per scorrere la pagina verso l'alto
      scrollToTop();
    });
  }
});

function scrollToTop(): void {
  window.scrollTo({ top: 0, behavior: 'smooth' });
}