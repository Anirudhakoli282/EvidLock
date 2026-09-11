import { Component, Input } from '@angular/core';
import { Corso } from '../../../model/Corso';
import { CorsiService } from '../../../service/corsi/corsi.service';

@Component({
  selector: 'app-corsi-card',
  templateUrl: './corsi-card.component.html',
  styleUrl: './corsi-card.component.css'
})
export class CorsiCardComponent {
  @Input() corso!: Corso;

  constructor(private corsiService: CorsiService) {}

}
