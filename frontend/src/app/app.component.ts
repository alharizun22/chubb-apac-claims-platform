import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './components/header/header.component';
import { ClaimantDashboardComponent } from './components/claimant-dashboard/claimant-dashboard.component';
import { OfficerWorkbenchComponent } from './components/officer-workbench/officer-workbench.component';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    ClaimantDashboardComponent,
    OfficerWorkbenchComponent
  ],
  template: `
    <div class="app-layout">
      <app-header></app-header>
      <main class="main-content">
        <!-- Render Claimant Dashboard when role is CLAIMANT -->
        <app-claimant-dashboard *ngIf="authService.isClaimant()"></app-claimant-dashboard>

        <!-- Render Officer Workbench when role is OFFICER -->
        <app-officer-workbench *ngIf="authService.isOfficer()"></app-officer-workbench>
      </main>
      <footer class="app-footer">
        <div class="footer-content">
          <span>Chubb APAC Engineering • Young Graduate Program Technical Assessment</span>
          <span>Java 21 • Spring Boot 3.3 • Kafka (Redpanda) • PostgreSQL 16 • Angular 17</span>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    .app-layout {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    .main-content {
      flex: 1;
      padding-bottom: 2rem;
    }
    .app-footer {
      background: #0f172a;
      color: #94a3b8;
      font-size: 0.775rem;
      padding: 1.25rem 1.5rem;
      border-top: 1px solid #1e293b;
      margin-top: auto;
    }
    .footer-content {
      max-width: 1380px;
      margin: 0 auto;
      display: flex;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 0.5rem;
    }
  `]
})
export class AppComponent {
  constructor(public authService: AuthService) {}
}
