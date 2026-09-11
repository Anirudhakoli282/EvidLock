import { Component, Input } from '@angular/core';
import { Corso } from '../../../model/Corso';

@Component({
  selector: 'app-riepilogo-corsi',
  templateUrl: './riepilogo-corsi.component.html',
  styleUrl: './riepilogo-corsi.component.css'
})
export class RiepilogoCorsiComponent {
  @Input() corsi!: Corso[];
}
