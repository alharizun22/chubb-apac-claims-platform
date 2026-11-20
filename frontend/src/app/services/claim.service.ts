import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Claim, ClaimDetail, Market, ClaimStatus, SubmitClaimRequest } from '../models/claim.model';

@Injectable({
  providedIn: 'root'
})
export class ClaimService {
  private readonly baseUrl = 'http://localhost:8080/api/claims';

  constructor(private http: HttpClient) {}

  public getClaims(market?: Market, status?: ClaimStatus, claimantId?: string): Observable<Claim[]> {
    let params = new HttpParams();
    if (market) params = params.set('market', market);
    if (status) params = params.set('status', status);
    if (claimantId) params = params.set('claimantId', claimantId);

    return this.http.get<Claim[]>(this.baseUrl, { params });
  }

  public getClaimById(id: string): Observable<ClaimDetail> {
    return this.http.get<ClaimDetail>(`${this.baseUrl}/${id}`);
  }

  public submitClaim(request: SubmitClaimRequest): Observable<ClaimDetail> {
    return this.http.post<ClaimDetail>(this.baseUrl, request);
  }
}
