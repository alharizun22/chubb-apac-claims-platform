import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService, UserProfile } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { NotificationRecord } from '../../models/claim.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <header class="app-header">
      <div class="header-container">
        <div class="brand">
          <div class="brand-logo">CHUBB</div>
          <div class="brand-title">
            <span class="brand-name">APAC Claims Platform</span>
            <span class="brand-env">DEV / LOCAL</span>
          </div>
        </div>

        <div class="header-controls">
          <!-- Role Switcher -->
          <div class="role-switcher">
            <span class="role-label">Active Persona:</span>
            <div class="switcher-buttons">
              <button
                type="button"
                class="switch-btn"
                [class.active]="authService.isClaimant()"
                (click)="authService.switchToClaimant()">
                <span class="icon">👤</span> Claimant
              </button>
              <button
                type="button"
                class="switch-btn"
                [class.active]="authService.isOfficer()"
                (click)="authService.switchToOfficer()">
                <span class="icon">🛡️</span> Claims Officer
              </button>
            </div>
          </div>

          <!-- Notification Bell -->
          <div class="notification-wrapper">
            <button class="notification-btn" (click)="toggleNotifications()">
              🔔
              <span *ngIf="notifications.length > 0" class="notification-count">{{ notifications.length }}</span>
            </button>

            <!-- Notifications Dropdown -->
            <div *ngIf="showNotifications" class="notification-dropdown">
              <div class="dropdown-header">
                <h4>Simulated Alerts (Kafka)</h4>
                <button class="btn-clear" (click)="fetchNotifications()">Refresh</button>
              </div>
              <div class="notification-list">
                <div *ngIf="notifications.length === 0" class="empty-notifications">
                  No alerts logged yet.
                </div>
                <div *ngFor="let n of notifications" class="notification-item">
                  <div class="notif-top">
                    <span class="notif-badge">{{ n.channel }}</span>
                    <span class="notif-time">{{ n.sentAt | date:'HH:mm:ss' }}</span>
                  </div>
                  <div class="notif-title">{{ n.title }}</div>
                  <div class="notif-msg">{{ n.message }}</div>
                  <div class="notif-ref">Ref: {{ n.referenceNumber }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Active User Badge -->
          <div class="user-badge" *ngIf="authService.currentUser$ | async as user">
            <div class="user-avatar">{{ user.role === 'CLAIMANT' ? 'CL' : 'OF' }}</div>
            <div class="user-info">
              <div class="user-name">{{ user.name }}</div>
              <div class="user-role">{{ user.role }} • {{ user.userId }}</div>
            </div>
          </div>
        </div>
      </div>
    </header>
  `,
  styles: [`
    .app-header {
      background: #1e1035;
      color: white;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
      position: sticky;
      top: 0;
      z-index: 500;
    }
    .header-container {
      max-width: 1380px;
      margin: 0 auto;
      padding: 0.85rem 1.5rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .brand-logo {
      font-size: 1.5rem;
      font-weight: 900;
      letter-spacing: 0.12em;
      color: #ffffff;
      background: #a3238e;
      padding: 0.2rem 0.6rem;
      border-radius: 4px;
    }
    .brand-title {
      display: flex;
      flex-direction: column;
    }
    .brand-name {
      font-size: 1.05rem;
      font-weight: 600;
      color: #ffffff;
    }
    .brand-env {
      font-size: 0.65rem;
      color: #94a3b8;
      font-family: monospace;
      letter-spacing: 0.05em;
    }
    .header-controls {
      display: flex;
      align-items: center;
      gap: 1.25rem;
    }
    .role-switcher {
      display: flex;
      align-items: center;
      gap: 0.6rem;
      background: rgba(255, 255, 255, 0.08);
      padding: 0.25rem 0.5rem;
      border-radius: 8px;
    }
    .role-label {
      font-size: 0.75rem;
      color: #cbd5e1;
      font-weight: 500;
    }
    .switcher-buttons {
      display: flex;
      gap: 0.25rem;
    }
    .switch-btn {
      background: transparent;
      border: 1px solid transparent;
      color: #cbd5e1;
      padding: 0.35rem 0.75rem;
      font-size: 0.8rem;
      font-weight: 500;
      border-radius: 6px;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 0.35rem;
      transition: all 0.15s ease-in-out;
    }
    .switch-btn:hover {
      color: white;
      background: rgba(255, 255, 255, 0.12);
    }
    .switch-btn.active {
      background: #a3238e;
      color: white;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
    }
    .notification-wrapper {
      position: relative;
    }
    .notification-btn {
      background: rgba(255, 255, 255, 0.1);
      border: none;
      color: white;
      font-size: 1.1rem;
      padding: 0.4rem 0.6rem;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
    }
    .notification-count {
      position: absolute;
      top: -4px;
      right: -4px;
      background: #ef4444;
      color: white;
      font-size: 0.65rem;
      font-weight: 700;
      padding: 0.1rem 0.35rem;
      border-radius: 9999px;
    }
    .notification-dropdown {
      position: absolute;
      right: 0;
      top: 2.5rem;
      background: white;
      color: #0f172a;
      width: 340px;
      border-radius: 8px;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.25);
      border: 1px solid #e2e8f0;
      z-index: 1000;
      max-height: 400px;
      overflow-y: auto;
    }
    .dropdown-header {
      padding: 0.75rem 1rem;
      border-bottom: 1px solid #e2e8f0;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .dropdown-header h4 {
      font-size: 0.85rem;
      color: #1e1035;
      margin: 0;
    }
    .btn-clear {
      background: none;
      border: none;
      color: #a3238e;
      font-size: 0.75rem;
      cursor: pointer;
      font-weight: 600;
    }
    .notification-list {
      padding: 0.5rem;
    }
    .empty-notifications {
      padding: 1.5rem;
      text-align: center;
      color: #64748b;
      font-size: 0.85rem;
    }
    .notification-item {
      padding: 0.6rem;
      border-bottom: 1px solid #f1f5f9;
      font-size: 0.8rem;
    }
    .notif-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.2rem;
    }
    .notif-badge {
      background: #e2e8f0;
      padding: 0.1rem 0.35rem;
      border-radius: 4px;
      font-size: 0.65rem;
      font-weight: 600;
    }
    .notif-time {
      font-size: 0.65rem;
      color: #94a3b8;
    }
    .notif-title {
      font-weight: 600;
      color: #1e1035;
      margin-bottom: 0.15rem;
    }
    .notif-msg {
      color: #475569;
      font-size: 0.75rem;
      line-height: 1.3;
    }
    .notif-ref {
      font-family: monospace;
      font-size: 0.7rem;
      color: #64748b;
      margin-top: 0.2rem;
    }
    .user-badge {
      display: flex;
      align-items: center;
      gap: 0.6rem;
      border-left: 1px solid rgba(255, 255, 255, 0.15);
      padding-left: 1rem;
    }
    .user-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #a3238e;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.75rem;
      font-weight: 700;
    }
    .user-info {
      display: flex;
      flex-direction: column;
    }
    .user-name {
      font-size: 0.825rem;
      font-weight: 600;
    }
    .user-role {
      font-size: 0.7rem;
      color: #cbd5e1;
    }
  `]
})
export class HeaderComponent implements OnInit {
  public showNotifications = false;
  public notifications: NotificationRecord[] = [];

  constructor(
    public authService: AuthService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.fetchNotifications();
    // Refresh alerts periodically
    setInterval(() => this.fetchNotifications(), 10000);
  }

  public toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      this.fetchNotifications();
    }
  }

  public fetchNotifications(): void {
    this.notificationService.getNotifications().subscribe({
      next: (data) => {
        this.notifications = data || [];
      },
      error: () => {}
    });
  }
}
