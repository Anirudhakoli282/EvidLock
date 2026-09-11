import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-corsi-icon',
  templateUrl: './corsi-icon.component.html',
  styleUrl: './corsi-icon.component.css'
})
export class CorsiIconComponent {

  @Input() categoria: string = '';

}
