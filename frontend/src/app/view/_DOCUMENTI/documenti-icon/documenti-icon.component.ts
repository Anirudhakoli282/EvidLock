import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-documenti-icon',
  templateUrl: './documenti-icon.component.html',
  styleUrl: './documenti-icon.component.css'
})
export class DocumentiIconComponent {
  @Input() formato: string = '';

  getIconClass(): string {
    // Mappa il formato a un'icona appropriata
    switch (this.formato.toLowerCase()) {
      case 'pdf':
        return 'bi-file-earmark-pdf';
      case 'png':
      case 'jpg':
        return 'bi-file-image';
      case 'txt':
        return 'bi-file-text';
      case 'doc':
      case 'docx':
        return 'bi-file-word';
      case 'xls':
      case 'xlsx':
        return 'bi-file-excel';
      case 'ppt':
      case 'pptx':
        return 'bi-file-powerpoint';
      case 'zip':
      case 'rar':
      case 'tar':
        return 'bi-file-zip';
      default:
        return 'bi-file-earmark';
    }
  }
}
