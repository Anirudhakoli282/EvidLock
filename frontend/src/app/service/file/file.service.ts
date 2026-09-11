import { Injectable } from '@angular/core';

import { Settings } from '../../Settings';
import { AuthService } from '../auth/auth.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(
    private auth: AuthService,
    private http: HttpClient
  ) { }

  getFile(path: String) : Observable<Blob> {
    return this.http.get(Settings.API_ENDPOINT + 'get-file?path=' + path, { responseType: 'blob', headers: this.auth.headers });
  }
}
