import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AssessmentService } from '../../services/assessment.service';
import { ExposureService } from '../../services/exposure.service';
import { ClaimService } from '../../services/claim.service';
import { AuthService } from '../../services/auth.service';
import { Claim, ClaimDetail, Market, MarketExposure } from '../../models/claim.model';
import { ClaimTimelineComponent } from '../claim-timeline/claim-timeline.component';

@Component({
  selector: 'app-officer-workbench',
  standalone: true,
  imports: [CommonModule, FormsModule, ClaimTimelineComponent],
  template: `
    <div class="container">
      <div class="workbench-header">
        <div>
          <h2>Claims Officer Workbench</h2>
          <p class="subtitle">Triage incoming claims, conduct assessments, authorize payouts, and monitor regional exposure.</p>
        </div>
        <div class="workbench-nav">
          <button
            class="tab-btn"
            [class.active]="activeTab === 'unassigned'"
            (click)="setTab('unassigned')">
            📥 Unassigned Queue
            <span class="tab-badge" *ngIf="unassignedClaims.length > 0">{{ unassignedClaims.length }}</span>
          </button>
          <button
            class="tab-btn"
            [class.active]="activeTab === 'assigned'"
            (click)="setTab('assigned')">
            📝 My Review Queue
            <span class="tab-badge" *ngIf="assignedClaims.length > 0">{{ assignedClaims.length }}</span>
          </button>
          <button
            class="tab-btn"
            [class.active]="activeTab === 'exposure'"
            (click)="setTab('exposure')">
            📊 Regional Exposure (CQRS)
          </button>
        </div>
      </div>

      <!-- Feedback Alerts -->
      <div *ngIf="alertMessage" class="alert" [ngClass]="'alert-' + alertType">
        <span>{{ alertMessage }}</span>
        <button class="btn btn-sm btn-secondary" (click)="alertMessage = null">✕</button>
      </div>

      <!-- TAB 1: UNASSIGNED CLAIMS QUEUE -->
      <div *ngIf="activeTab === 'unassigned'" class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">Incoming Unassigned Claims (Status: SUBMITTED)</h3>
            <span class="text-muted">Claims waiting to be picked up by an APAC Claims Officer</span>
          </div>
          <button class="btn btn-secondary btn-sm" (click)="loadUnassigned()">
            🔄 Refresh Queue
          </button>
        </div>

        <div *ngIf="loading" class="loading-state">
          Loading unassigned claims...
        </div>

        <div *ngIf="!loading && unassignedClaims.length === 0" class="empty-state">
          🎉 All incoming claims have been assigned! Check back later or test by filing a new claim as a Claimant.
        </div>

        <div class="table-responsive" *ngIf="!loading && unassignedClaims.length > 0">
          <table class="table">
            <thead>
              <tr>
                <th>Reference #</th>
                <th>Market</th>
                <th>Type</th>
                <th>Policy #</th>
                <th>Claimant</th>
                <th>Incident Date</th>
                <th>Estimated Loss</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let c of unassignedClaims">
                <td><strong>{{ c.referenceNumber }}</strong></td>
                <td><span class="market-pill">{{ c.market }}</span></td>
                <td>{{ c.claimType }}</td>
                <td><code>{{ c.policyNumber }}</code></td>
                <td>{{ c.claimantName }}</td>
                <td>{{ c.incidentDate }}</td>
                <td><strong>{{ c.estimatedAmount | currency:'USD':'symbol':'1.2-2' }}</strong></td>
                <td>
                  <button class="btn btn-sm btn-primary" (click)="onPickup(c.id)">
                    📥 Pick Up
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 2: MY ASSIGNED REVIEW QUEUE -->
      <div *ngIf="activeTab === 'assigned'" class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">My Review Queue (UNDER_REVIEW / PENDING_INFO)</h3>
            <span class="text-muted">Active claims assigned to {{ authService.currentUser.userId }}</span>
          </div>
          <button class="btn btn-secondary btn-sm" (click)="loadAssigned()">
            🔄 Refresh Queue
          </button>
        </div>

        <div *ngIf="loading" class="loading-state">
          Loading your assigned claims...
        </div>

        <div *ngIf="!loading && assignedClaims.length === 0" class="empty-state">
          You currently have no open claims assigned. Pick up new claims from the "Unassigned Queue" tab!
        </div>

        <div class="table-responsive" *ngIf="!loading && assignedClaims.length > 0">
          <table class="table">
            <thead>
              <tr>
                <th>Reference #</th>
                <th>Market</th>
                <th>Type</th>
                <th>Claimant</th>
                <th>Estimated</th>
                <th>Status</th>
                <th>Assessment Decision Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let c of assignedClaims">
                <td><strong>{{ c.referenceNumber }}</strong></td>
                <td><span class="market-pill">{{ c.market }}</span></td>
                <td>{{ c.claimType }}</td>
                <td>{{ c.claimantName }}</td>
                <td><strong>{{ c.estimatedAmount | currency:'USD':'symbol':'1.2-2' }}</strong></td>
                <td>
                  <span class="badge badge-{{ c.status }}">{{ c.status }}</span>
                </td>
                <td>
                  <div class="action-buttons-group">
                    <button class="btn btn-sm btn-secondary" (click)="openTimeline(c.id)">
                      📜 History
                    </button>
                    <button
                      class="btn btn-sm btn-warning"
                      [disabled]="c.status === 'PENDING_INFO'"
                      (click)="openRequestInfoModal(c)">
                      ❓ Request Info
                    </button>
                    <button
                      class="btn btn-sm btn-success"
                      [disabled]="c.status === 'PENDING_INFO'"
                      (click)="openSettleModal(c)">
                      ✅ Settle
                    </button>
                    <button
                      class="btn btn-sm btn-danger"
                      [disabled]="c.status === 'PENDING_INFO'"
                      (click)="openRejectModal(c)">
                      ❌ Reject
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 3: REGIONAL LIABILITY EXPOSURE -->
      <div *ngIf="activeTab === 'exposure'">
        <!-- Aggregate Bar -->
        <div class="exposure-metrics-banner">
          <div class="metric-card">
            <span class="metric-title">Total Open Claims (APAC)</span>
            <span class="metric-value">{{ totalOpenClaims }}</span>
          </div>
          <div class="metric-card">
            <span class="metric-title">Total Estimated Exposure</span>
            <span class="metric-value text-warning">{{ totalEstimatedExposure | currency:'USD':'symbol':'1.2-2' }}</span>
          </div>
          <div class="metric-card">
            <span class="metric-title">Total Settled Payouts</span>
            <span class="metric-value text-success">{{ totalSettledPayout | currency:'USD':'symbol':'1.2-2' }}</span>
          </div>
          <div class="metric-refresh">
            <button class="btn btn-secondary btn-sm" (click)="loadExposures()">🔄 Refresh Metrics</button>
          </div>
        </div>

        <!-- Per-Market Exposure Grid -->
        <div class="card">
          <div class="card-header">
            <div>
              <h3 class="card-title">APAC Regional Exposure Projections (CQRS Read Table)</h3>
              <span class="text-muted">Updated asynchronously via Kafka consumers upon claim lifecycle events</span>
            </div>
          </div>

          <div class="market-grid">
            <div *ngFor="let exp of exposures" class="market-card">
              <div class="market-card-top">
                <span class="market-badge-lg">{{ exp.market }}</span>
                <span class="market-status">{{ getMarketName(exp.market) }}</span>
              </div>
              <div class="market-stat">
                <span class="stat-label">Open Claims:</span>
                <span class="stat-num">{{ exp.openClaimCount }}</span>
              </div>
              <div class="market-stat">
                <span class="stat-label">Est. Exposure:</span>
                <span class="stat-num text-warning">{{ exp.totalEstimatedExposure | currency:'USD':'symbol':'1.2-2' }}</span>
              </div>
              <div class="market-stat">
                <span class="stat-label">Settled Payout:</span>
                <span class="stat-num text-success">{{ exp.totalSettledPayout | currency:'USD':'symbol':'1.2-2' }}</span>
              </div>
              <div class="market-updated">
                Updated: {{ exp.lastUpdatedAt | date:'mediumTime' }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- MODAL: REQUEST INFO -->
      <div *ngIf="modalMode === 'requestInfo' && activeModalClaim" class="modal-overlay" (click)="closeModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3>Request Additional Information</h3>
            <button class="btn btn-sm btn-secondary" (click)="closeModal()">✕</button>
          </div>
          <p class="modal-desc">
            Request supplementary documentation or clarification from claimant for
            <strong>{{ activeModalClaim.referenceNumber }}</strong>. This will transition status to <code>PENDING_INFO</code>.
          </p>
          <div class="form-group">
            <label class="form-label">Note / Details Requested from Claimant *</label>
            <textarea [(ngModel)]="requestNote" class="form-control" rows="3" placeholder="Specify required documents, photos, or police reports..."></textarea>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="closeModal()">Cancel</button>
            <button class="btn btn-warning" [disabled]="!requestNote.trim() || actionSubmitting" (click)="submitRequestInfo()">
              {{ actionSubmitting ? 'Sending Request...' : 'Send Request to Claimant' }}
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL: SETTLE CLAIM -->
      <div *ngIf="modalMode === 'settle' && activeModalClaim" class="modal-overlay" (click)="closeModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3>Authorize Settlement & Payout</h3>
            <button class="btn btn-sm btn-secondary" (click)="closeModal()">✕</button>
          </div>
          <p class="modal-desc">
            Approve claim <strong>{{ activeModalClaim.referenceNumber }}</strong>.
            Estimated loss was <strong>{{ activeModalClaim.estimatedAmount | currency:'USD':'symbol':'1.2-2' }}</strong>.
          </p>
          <div class="form-group">
            <label class="form-label">Authorized Settlement Amount (USD/Local) *</label>
            <input type="number" step="0.01" min="1" [(ngModel)]="settleAmount" class="form-control" placeholder="e.g. 2400.00">
          </div>
          <div class="form-group">
            <label class="form-label">Settlement Note / Deductible Explanation</label>
            <textarea [(ngModel)]="settleNote" class="form-control" rows="2" placeholder="e.g. Full amount approved less standard policy deductible."></textarea>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="closeModal()">Cancel</button>
            <button class="btn btn-success" [disabled]="!settleAmount || settleAmount <= 0 || actionSubmitting" (click)="submitSettle()">
              {{ actionSubmitting ? 'Authorizing Payout...' : 'Authorize Settlement' }}
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL: REJECT CLAIM -->
      <div *ngIf="modalMode === 'reject' && activeModalClaim" class="modal-overlay" (click)="closeModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3>Reject Claim</h3>
            <button class="btn btn-sm btn-secondary" (click)="closeModal()">✕</button>
          </div>
          <p class="modal-desc text-danger">
            Deny claim <strong>{{ activeModalClaim.referenceNumber }}</strong>. This action is terminal and cannot be reversed.
          </p>
          <div class="form-group">
            <label class="form-label">Rejection Reason *</label>
            <textarea [(ngModel)]="rejectionReason" class="form-control" rows="3" placeholder="State policy exclusion clause or factual reason for rejection..."></textarea>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="closeModal()">Cancel</button>
            <button class="btn btn-danger" [disabled]="!rejectionReason.trim() || actionSubmitting" (click)="submitReject()">
              {{ actionSubmitting ? 'Rejecting Claim...' : 'Confirm Rejection' }}
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL: TIMELINE DETAILS -->
      <div *ngIf="modalMode === 'timeline' && detailClaim" class="modal-overlay" (click)="closeModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3>Claim History: {{ detailClaim.referenceNumber }}</h3>
            <button class="btn btn-sm btn-secondary" (click)="closeModal()">✕</button>
          </div>
          <div class="detail-grid" style="font-size: 0.85rem; margin-bottom: 1rem;">
            <div><strong>Market:</strong> {{ detailClaim.market }}</div>
            <div><strong>Type:</strong> {{ detailClaim.claimType }}</div>
            <div><strong>Policy #:</strong> {{ detailClaim.policyNumber }}</div>
            <div><strong>Claimant:</strong> {{ detailClaim.claimantName }}</div>
            <div><strong>Estimated:</strong> {{ detailClaim.estimatedAmount | currency:'USD':'symbol':'1.2-2' }}</div>
            <div *ngIf="detailClaim.settledAmount"><strong>Settled:</strong> <span class="text-success">{{ detailClaim.settledAmount | currency:'USD':'symbol':'1.2-2' }}</span></div>
            <div><strong>Status:</strong> <span class="badge badge-{{ detailClaim.status }}">{{ detailClaim.status }}</span></div>
          </div>
          <app-claim-timeline [activities]="detailClaim.activities"></app-claim-timeline>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="closeModal()">Close</button>
          </div>
        </div>
      </div>

    </div>
  `,
  styles: [`
    .workbench-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .subtitle {
      color: #64748b;
      font-size: 0.9rem;
    }
    .workbench-nav {
      display: flex;
      gap: 0.5rem;
      background: #f1f5f9;
      padding: 0.25rem;
      border-radius: 8px;
    }
    .tab-btn {
      padding: 0.5rem 1rem;
      font-size: 0.85rem;
      font-weight: 600;
      border: none;
      background: transparent;
      border-radius: 6px;
      cursor: pointer;
      color: #475569;
      display: flex;
      align-items: center;
      gap: 0.4rem;
      transition: all 0.15s;
    }
    .tab-btn:hover {
      color: #1e1035;
    }
    .tab-btn.active {
      background: white;
      color: #a3238e;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }
    .tab-badge {
      background: #a3238e;
      color: white;
      font-size: 0.7rem;
      padding: 0.1rem 0.45rem;
      border-radius: 9999px;
    }
    .loading-state, .empty-state {
      padding: 2.5rem;
      text-align: center;
      color: #64748b;
      font-size: 0.95rem;
    }
    .action-buttons-group {
      display: flex;
      gap: 0.35rem;
      flex-wrap: wrap;
    }
    .btn-warning {
      background: #f59e0b;
      color: white;
    }
    .btn-warning:hover {
      background: #d97706;
    }
    /* Exposure Cards */
    .exposure-metrics-banner {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
    }
    .metric-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1.25rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }
    .metric-title {
      font-size: 0.8rem;
      color: #64748b;
      font-weight: 500;
    }
    .metric-value {
      font-size: 1.75rem;
      font-weight: 700;
      color: #1e1035;
    }
    .metric-refresh {
      display: flex;
      align-items: center;
      justify-content: flex-end;
    }
    .market-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1.25rem;
    }
    .market-card {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      padding: 1.25rem;
      transition: transform 0.15s, box-shadow 0.15s;
    }
    .market-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    }
    .market-card-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.75rem;
      padding-bottom: 0.5rem;
      border-bottom: 1px solid #e2e8f0;
    }
    .market-badge-lg {
      font-family: monospace;
      font-weight: 900;
      font-size: 1.1rem;
      background: #1e1035;
      color: white;
      padding: 0.2rem 0.6rem;
      border-radius: 6px;
    }
    .market-status {
      font-size: 0.8rem;
      font-weight: 600;
      color: #64748b;
    }
    .market-stat {
      display: flex;
      justify-content: space-between;
      font-size: 0.85rem;
      margin-bottom: 0.4rem;
    }
    .stat-label {
      color: #64748b;
    }
    .stat-num {
      font-weight: 700;
    }
    .market-updated {
      font-size: 0.7rem;
      color: #94a3b8;
      margin-top: 0.75rem;
      text-align: right;
    }
    .modal-desc {
      font-size: 0.875rem;
      color: #475569;
      margin-bottom: 1rem;
    }
  `]
})
export class OfficerWorkbenchComponent implements OnInit {
  public activeTab: 'unassigned' | 'assigned' | 'exposure' = 'unassigned';
  public loading = false;
  public actionSubmitting = false;

  public unassignedClaims: Claim[] = [];
  public assignedClaims: Claim[] = [];
  public exposures: MarketExposure[] = [];

  public alertMessage: string | null = null;
  public alertType: 'success' | 'danger' | 'warning' = 'success';

  // Modals state
  public modalMode: 'requestInfo' | 'settle' | 'reject' | 'timeline' | null = null;
  public activeModalClaim: Claim | null = null;
  public detailClaim: ClaimDetail | null = null;

  public requestNote = '';
  public settleAmount: number | null = null;
  public settleNote = '';
  public rejectionReason = '';

  constructor(
    private assessmentService: AssessmentService,
    private exposureService: ExposureService,
    private claimService: ClaimService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  public setTab(tab: 'unassigned' | 'assigned' | 'exposure'): void {
    this.activeTab = tab;
    if (tab === 'unassigned') this.loadUnassigned();
    if (tab === 'assigned') this.loadAssigned();
    if (tab === 'exposure') this.loadExposures();
  }

  public loadAll(): void {
    this.loadUnassigned();
    this.loadAssigned();
    this.loadExposures();
  }

  public loadUnassigned(): void {
    this.loading = true;
    this.assessmentService.getUnassignedClaims().subscribe({
      next: (data) => {
        this.unassignedClaims = data || [];
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.showAlert('Failed to load unassigned queue: ' + (err.error?.message || err.message), 'danger');
      }
    });
  }

  public loadAssigned(): void {
    this.loading = true;
    this.assessmentService.getAssignedClaims().subscribe({
      next: (data) => {
        this.assignedClaims = data || [];
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.showAlert('Failed to load assigned queue: ' + (err.error?.message || err.message), 'danger');
      }
    });
  }

  public loadExposures(): void {
    this.exposureService.getAllExposures().subscribe({
      next: (data) => {
        this.exposures = data || [];
      },
      error: (err) => {
        this.showAlert('Failed to load exposure projections: ' + (err.error?.message || err.message), 'danger');
      }
    });
  }

  public onPickup(claimId: string): void {
    this.assessmentService.pickupClaim(claimId).subscribe({
      next: (updated) => {
        this.showAlert(`Claim ${updated.referenceNumber} picked up! Moved to My Review Queue.`, 'success');
        this.loadUnassigned();
        this.loadAssigned();
        this.loadExposures();
      },
      error: (err) => {
        this.showAlert('Pickup failed: ' + (err.error?.message || err.message), 'danger');
      }
    });
  }

  public openRequestInfoModal(claim: Claim): void {
    this.activeModalClaim = claim;
    this.requestNote = '';
    this.modalMode = 'requestInfo';
  }

  public submitRequestInfo(): void {
    if (!this.activeModalClaim || !this.requestNote.trim()) return;

    this.actionSubmitting = true;
    this.assessmentService.requestInfo(this.activeModalClaim.id, { note: this.requestNote }).subscribe({
      next: (updated) => {
        this.actionSubmitting = false;
        this.closeModal();
        this.showAlert(`Information requested for ${updated.referenceNumber}. Status set to PENDING_INFO.`, 'warning');
        this.loadAssigned();
      },
      error: (err) => {
        this.actionSubmitting = false;
        this.showAlert('Failed to request info: ' + (err.error?.message || err.message), 'danger');
      }
    });
  }

  public openSettleModal(claim: Claim): void {
    this.activeModalClaim = claim;
    this.settleAmount = claim.estimatedAmount;
    this.settleNote = '';
    this.modalMode = 'settle';
  }

  public submitSettle(): void {
    if (!this.activeModalClaim || !this.settleAmount || this.settleAmount <= 0) return;

    this.actionSubmitting = true;
    this.assessmentService.settleClaim(this.activeModalClaim.id, {
      settledAmount: this.settleAmount,
      note: this.settleNote
    }).subscribe({
      next: (updated) => {
        this.actionSubmitting = false;
        this.closeModal();
        this.showAlert(`Claim ${updated.referenceNumber} SETTLED for ${updated.settledAmount}! Payout logged to Kafka.`, 'success');
        this.loadAssigned();
        this.loadExposures();
      },
      error: (err) => {
        this.actionSubmitting = false;
        this.showAlert('Settlement failed: ' + (err.error?.message || err.message), 'danger');
      }
    });
  }

  public openRejectModal(claim: Claim): void {
    this.activeModalClaim = claim;
    this.rejectionReason = '';
    this.modalMode = 'reject';
  }

  public submitReject(): void {
    if (!this.activeModalClaim || !this.rejectionReason.trim()) return;

    this.actionSubmitting = true;
    this.assessmentService.rejectClaim(this.activeModalClaim.id, {
      rejectionReason: this.rejectionReason
    }).subscribe({
      next: (updated) => {
        this.actionSubmitting = false;
        this.closeModal();
        this.showAlert(`Claim ${updated.referenceNumber} REJECTED. Decision event emitted.`, 'danger');
        this.loadAssigned();
        this.loadExposures();
      },
      error: (err) => {
        this.actionSubmitting = false;
        this.showAlert('Rejection failed: ' + (err.error?.message || err.message), 'danger');
      }
    });
  }

  public openTimeline(claimId: string): void {
    this.claimService.getClaimById(claimId).subscribe({
      next: (detail) => {
        this.detailClaim = detail;
        this.modalMode = 'timeline';
      },
      error: (err) => {
        this.showAlert('Could not load history: ' + (err.error?.message || err.message), 'danger');
      }
    });
  }

  public closeModal(): void {
    this.modalMode = null;
    this.activeModalClaim = null;
    this.detailClaim = null;
  }

  public get totalOpenClaims(): number {
    return this.exposures.reduce((sum, e) => sum + (e.openClaimCount || 0), 0);
  }

  public get totalEstimatedExposure(): number {
    return this.exposures.reduce((sum, e) => sum + (e.totalEstimatedExposure || 0), 0);
  }

  public get totalSettledPayout(): number {
    return this.exposures.reduce((sum, e) => sum + (e.totalSettledPayout || 0), 0);
  }

  public getMarketName(code: string): string {
    switch (code) {
      case 'MY': return 'Malaysia';
      case 'SG': return 'Singapore';
      case 'TH': return 'Thailand';
      case 'HK': return 'Hong Kong';
      case 'AU': return 'Australia';
      case 'JP': return 'Japan';
      default: return code;
    }
  }

  private showAlert(msg: string, type: 'success' | 'danger' | 'warning'): void {
    this.alertMessage = msg;
    this.alertType = type;
    setTimeout(() => {
      if (this.alertMessage === msg) this.alertMessage = null;
    }, 6000);
  }
}
