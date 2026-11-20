import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { UserRole } from '../models/claim.model';

export interface UserProfile {
  userId: string;
  name: string;
  role: UserRole;
  market?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private static readonly STORAGE_KEY = 'chubb_user_profile';

  private readonly claimantPreset: UserProfile = {
    userId: 'claimant-my-01',
    name: 'Nurul Ain (Claimant)',
    role: 'CLAIMANT',
    market: 'MY'
  };

  private readonly officerPreset: UserProfile = {
    userId: 'officer-sarah',
    name: 'Sarah Tan (Claims Officer)',
    role: 'OFFICER'
  };

  private currentUserSubject: BehaviorSubject<UserProfile>;
  public currentUser$: Observable<UserProfile>;

  constructor() {
    const saved = localStorage.getItem(AuthService.STORAGE_KEY);
    const initial = saved ? JSON.parse(saved) : this.claimantPreset;
    this.currentUserSubject = new BehaviorSubject<UserProfile>(initial);
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  public get currentUser(): UserProfile {
    return this.currentUserSubject.value;
  }

  public switchToClaimant(): void {
    this.setUser(this.claimantPreset);
  }

  public switchToOfficer(): void {
    this.setUser(this.officerPreset);
  }

  public setUser(user: UserProfile): void {
    localStorage.setItem(AuthService.STORAGE_KEY, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  public isClaimant(): boolean {
    return this.currentUser.role === 'CLAIMANT';
  }

  public isOfficer(): boolean {
    return this.currentUser.role === 'OFFICER';
  }
}
