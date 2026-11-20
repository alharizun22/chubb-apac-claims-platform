import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Claim,
  ClaimDetail,
  RequestInfoRequest,
  ProvideInfoRequest,
  SettleClaimRequest,
  RejectClaimRequest
} from '../models/claim.model';

@Injectable({
  providedIn: 'root'
})
export class AssessmentService {
  private readonly baseUrl = 'http://localhost:8080/api/assessment';

  constructor(private http: HttpClient) {}

  public getUnassignedClaims(): Observable<Claim[]> {
    return this.http.get<Claim[]>(`${this.baseUrl}/unassigned`);
  }

  public getAssignedClaims(): Observable<Claim[]> {
    return this.http.get<Claim[]>(`${this.baseUrl}/assigned`);
  }

  public pickupClaim(claimId: string): Observable<ClaimDetail> {
    return this.http.post<ClaimDetail>(`${this.baseUrl}/${claimId}/pickup`, {});
  }

  public requestInfo(claimId: string, request: RequestInfoRequest): Observable<ClaimDetail> {
    return this.http.post<ClaimDetail>(`${this.baseUrl}/${claimId}/request-info`, request);
  }

  public provideInfo(claimId: string, request: ProvideInfoRequest): Observable<ClaimDetail> {
    return this.http.post<ClaimDetail>(`${this.baseUrl}/${claimId}/provide-info`, request);
  }

  public settleClaim(claimId: string, request: SettleClaimRequest): Observable<ClaimDetail> {
    return this.http.post<ClaimDetail>(`${this.baseUrl}/${claimId}/settle`, request);
  }

  public rejectClaim(claimId: string, request: RejectClaimRequest): Observable<ClaimDetail> {
    return this.http.post<ClaimDetail>(`${this.baseUrl}/${claimId}/reject`, request);
  }
}
