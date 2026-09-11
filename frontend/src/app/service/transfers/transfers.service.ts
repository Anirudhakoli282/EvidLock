import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Settings } from '../../Settings';
import { AuthService } from '../auth/auth.service';
import { CustodyTransferRequest, TransferStepUpDto } from '../../model/CustodyTransferRequest';

@Injectable({
  providedIn: 'root'
})
export class TransfersService {

  constructor(private http: HttpClient, private authService: AuthService) { }

  getPendingTransfers(): Observable<CustodyTransferRequest[]> {
    return this.http.get<CustodyTransferRequest[]>(Settings.API_ENDPOINT + 'transfers/pending', { headers: this.authService.getHeaders() });
  }

  initiateTransfer(dto: TransferStepUpDto): Observable<CustodyTransferRequest> {
    return this.http.post<CustodyTransferRequest>(Settings.API_ENDPOINT + 'transfers/initiate', dto, { headers: this.authService.getHeaders() });
  }

  acceptTransfer(dto: TransferStepUpDto): Observable<CustodyTransferRequest> {
    return this.http.post<CustodyTransferRequest>(Settings.API_ENDPOINT + 'transfers/accept', dto, { headers: this.authService.getHeaders() });
  }

  rejectTransfer(requestId: number, reason?: string): Observable<any> {
    let url = `${Settings.API_ENDPOINT}transfers/reject?requestId=${requestId}`;
    if (reason) url += `&reason=${encodeURIComponent(reason)}`;
    return this.http.post(url, {}, { headers: this.authService.getHeaders(), responseType: 'text' });
  }
}
