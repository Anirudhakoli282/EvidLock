import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CommunicationService {
  private searchTextChanged = new Subject<string>();

  searchTextChanged$ = this.searchTextChanged.asObservable();

  setSearchText(text: string) {
    this.searchTextChanged.next(text);
  }
}
