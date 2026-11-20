import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NotificationRecord } from '../models/claim.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly baseUrl = 'http://localhost:8080/api/notifications';

  constructor(private http: HttpClient) {}

  public getNotifications(): Observable<NotificationRecord[]> {
    return this.http.get<NotificationRecord[]>(this.baseUrl);
  }
}
