import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MarketExposure, Market } from '../models/claim.model';

@Injectable({
  providedIn: 'root'
})
export class ExposureService {
  private readonly baseUrl = 'http://localhost:8080/api/exposure';

  constructor(private http: HttpClient) {}

  public getAllExposures(): Observable<MarketExposure[]> {
    return this.http.get<MarketExposure[]>(this.baseUrl);
  }

  public getMarketExposure(market: Market): Observable<MarketExposure> {
    return this.http.get<MarketExposure>(`${this.baseUrl}/${market}`);
  }
}
