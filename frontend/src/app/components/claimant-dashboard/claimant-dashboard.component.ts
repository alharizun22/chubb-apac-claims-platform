import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ClaimService } from '../../services/claim.service';
import { AssessmentService } from '../../services/assessment.service';
import { AuthService } from '../../services/auth.service';
import { Claim, ClaimDetail, Market, ClaimType } from '../../models/claim.model';
import { ClaimTimelineComponent } from '../claim-timeline/claim-timeline.component';

@Component({
  selector: 'app-claimant-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, ClaimTimelineComponent],
  template: `
    <div class="container">
      <div class="dashboard-header">
        <div>
          <h2>Claimant Portal</h2>
          <p class="subtitle">Submit first notice of loss (FNOL), track progress, and respond to claims officers.</p>
        </div>
        <button class="btn btn-primary" (click)="toggleSubmitForm()">
          {{ showSubmitForm ? 'Hide Form' : '➕ Submit New Claim (FNOL)' }}
        </button>
      </div>

      <!-- Feedback Alerts -->
      <div *ngIf="alertMessage" class="alert" [ngClass]="'alert-' + alertType">
        <span>{{ alertMessage }}</span>
        <button class="btn btn-sm btn-secondary" (click)="alertMessage = null">✕</button>
      </div>

      <!-- FNOL Submission Form -->
      <div *ngIf="showSubmitForm" class="card form-card">
        <div class="card-header">
          <h3 class="card-title">First Notice of Loss (FNOL) Submission</h3>
          <span class="market-pill">Markets: MY, SG, TH, HK, AU, JP</span>
        </div>

        <form [formGroup]="claimForm" (ngSubmit)="onSubmitClaim()">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Market *</label>
              <select class="form-select" formControlName="market">
                <option *ngFor="let m of markets" [value]="m">{{ m }}</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">Claim Type *</label>
              <select class="form-select" formControlName="claimType">
                <option value="MOTOR">Motor Vehicle</option>
                <option value="PROPERTY">Property / Home</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">Policy Number *</label>
              <input type="text" class="form-control" formControlName="policyNumber" placeholder="e.g. POL-MY-99281">
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Claimant Name *</label>
              <input type="text" class="form-control" formControlName="claimantName" placeholder="Full Name">
            </div>

            <div class="form-group">
              <label class="form-label">Incident Date *</label>
              <input type="date" class="form-control" formControlName="incidentDate">
            </div>

            <div class="form-group">
              <label class="form-label">Estimated Loss / Amount (USD/Local) *</label>
              <input type="number" step="0.01" min="1" class="form-control" formControlName="estimatedAmount" placeholder="e.g. 2500.00">
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Incident Description & Damage Details *</label>
            <textarea class="form-control" rows="3" formControlName="description" placeholder="Describe the incident, damage, and location..."></textarea>
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-secondary" (click)="showSubmitForm = false">Cancel</button>
            <button type="submit" class="btn btn-primary" [disabled]="claimForm.invalid || submitting">
              {{ submitting ? 'Submitting to Kafka...' : 'Submit Claim' }}
            </button>
          </div>
        </form>
      </div>

      <!-- My Claims Table -->
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">My Tracked Claims</h3>
            <span class="text-muted">Live status of your submitted claims</span>
          </div>
          <button class="btn btn-secondary btn-sm" (click)="loadClaims()">
            🔄 Refresh List
          </button>
        </div>

        <div *ngIf="loading" class="loading-state">
          Loading claims from Chubb API...
        </div>

        <div *ngIf="!loading && claims.length === 0" class="empty-state">
          No claims submitted yet under this claimant profile. Click "Submit New Claim" above to file your first claim!
        </div>

        <div class="table-responsive" *ngIf="!loading && claims.length > 0">
          <table class="table">
            <thead>
              <tr>
                <th>Reference #</th>
                <th>Market</th>
                <th>Type</th>
                <th>Policy #</th>
                <th>Incident Date</th>
                <th>Estimated Loss</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let c of claims">
                <td><strong>{{ c.referenceNumber }}</strong></td>
                <td><span class="market-pill">{{ c.market }}</span></td>
                <td>{{ c.claimType }}</td>
                <td><code>{{ c.policyNumber }}</code></td>
                <td>{{ c.incidentDate }}</td>
                <td><strong>{{ c.estimatedAmount | currency:'USD':'symbol':'1.2-2' }}</strong></td>
                <td>
                  <span class="badge badge-{{ c.status }}">
                    {{ c.status }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-secondary" (click)="openDetailModal(c.id)">
                    View & Respond
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Claim Detail & Response Modal -->
      <div *ngIf="selectedClaim" class="modal-overlay" (click)="closeDetailModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <div>
              <h3>{{ selectedClaim.referenceNumber }}</h3>
              <span class="badge badge-{{ selectedClaim.status }}">{{ selectedClaim.status }}</span>
            </div>
            <button class="btn btn-sm btn-secondary" (click)="closeDetailModal()">✕</button>
          </div>

          <!-- Claim Summary Card -->
          <div class="detail-summary">
            <div class="detail-grid">
              <div><span class="label">Market:</span> {{ selectedClaim.market }}</div>
              <div><span class="label">Type:</span> {{ selectedClaim.claimType }}</div>
              <div><span class="label">Policy Number:</span> {{ selectedClaim.policyNumber }}</div>
              <div><span class="label">Claimant:</span> {{ selectedClaim.claimantName }}</div>
              <div><span class="label">Incident Date:</span> {{ selectedClaim.incidentDate }}</div>
              <div><span class="label">Estimated Loss:</span> {{ selectedClaim.estimatedAmount | currency:'USD':'symbol':'1.2-2' }}</div>
              <div *ngIf="selectedClaim.settledAmount"><span class="label">Settled Amount:</span> <strong class="text-success">{{ selectedClaim.settledAmount | currency:'USD':'symbol':'1.2-2' }}</strong></div>
              <div *ngIf="selectedClaim.assignedOfficerId"><span class="label">Assigned Officer:</span> {{ selectedClaim.assignedOfficerId }}</div>
            </div>
            <div class="detail-desc">
              <span class="label">Description:</span>
              <p>{{ selectedClaim.description }}</p>
            </div>
          </div>

          <!-- Action Needed Alert if PENDING_INFO -->
          <div *ngIf="selectedClaim.status === 'PENDING_INFO'" class="action-card">
            <div class="action-header">
              <span class="action-icon">⚠️</span>
              <h4>Action Required: Claims Officer Requested Additional Information</h4>
            </div>
            <p class="action-subtext">Please review the officer's request note in the timeline below and provide the requested details to proceed with assessment.</p>
            
            <div class="form-group" style="margin-top: 0.75rem;">
              <label class="form-label">Your Response / Details *</label>
              <textarea [(ngModel)]="provideInfoNote" class="form-control" rows="3" placeholder="Provide requested policy documents, invoice, or clarification..."></textarea>
            </div>
            <button
              class="btn btn-primary"
              [disabled]="!provideInfoNote.trim() || submittingInfo"
              (click)="submitProvidedInfo()">
              {{ submittingInfo ? 'Submitting...' : 'Submit Information to Officer' }}
            </button>
          </div>

          <!-- Embedded Audit Timeline -->
          <app-claim-timeline [activities]="selectedClaim.activities"></app-claim-timeline>

          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="closeDetailModal()">Close</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-header {
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
    .form-card {
      border-top: 4px solid #a3238e;
    }
    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 0.75rem;
      margin-top: 1rem;
    }
    .loading-state, .empty-state {
      padding: 2.5rem;
      text-align: center;
      color: #64748b;
      font-size: 0.95rem;
    }
    .detail-summary {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      padding: 1rem;
      margin-bottom: 1rem;
    }
    .detail-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 0.5rem;
      font-size: 0.85rem;
      margin-bottom: 0.75rem;
    }
    .label {
      font-weight: 600;
      color: #475569;
    }
    .detail-desc {
      font-size: 0.85rem;
      border-top: 1px solid #e2e8f0;
      padding-top: 0.5rem;
    }
    .text-success {
      color: #059669;
    }
    .text-muted {
      color: #64748b;
      font-size: 0.8rem;
    }
    .action-card {
      background: #fffbeb;
      border: 1px solid #fde68a;
      border-radius: 8px;
      padding: 1rem;
      margin-bottom: 1rem;
    }
    .action-header {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #92400e;
      margin-bottom: 0.25rem;
    }
    .action-subtext {
      font-size: 0.825rem;
      color: #78350f;
    }
  `]
})
export class ClaimantDashboardComponent implements OnInit {
  public claims: Claim[] = [];
  public selectedClaim: ClaimDetail | null = null;
  public loading = false;
  public showSubmitForm = false;
  public submitting = false;
  public submittingInfo = false;
  public provideInfoNote = '';

  public alertMessage: string | null = null;
  public alertType: 'success' | 'danger' | 'warning' = 'success';

  public markets: Market[] = ['MY', 'SG', 'TH', 'HK', 'AU', 'JP'];
  public claimForm: FormGroup;

  constructor(
    private claimService: ClaimService,
    private assessmentService: AssessmentService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.claimForm = this.fb.group({
      market: ['MY', Validators.required],
      claimType: ['MOTOR', Validators.required],
      policyNumber: ['', Validators.required],
      claimantName: ['Nurul Ain', Validators.required],
      incidentDate: [new Date().toISOString().substring(0, 10), Validators.required],
      description: ['', [Validators.required, Validators.minLength(10)]],
      estimatedAmount: [1500.00, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.loadClaims();
  }

  public toggleSubmitForm(): void {
    this.showSubmitForm = !this.showSubmitForm;
  }

  public loadClaims(): void {
    this.loading = true;
    this.claimService.getClaims().subscribe({
      next: (data) => {
        this.claims = data || [];
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.showAlert('Failed to load claims: ' + (err.error?.message || err.message), 'danger');
      }
    });
  }

  public onSubmitClaim(): void {
    if (this.claimForm.invalid) return;

    this.submitting = true;
    this.claimService.submitClaim(this.claimForm.value).subscribe({
      next: (created) => {
        this.submitting = false;
        this.showSubmitForm = false;
        this.showAlert(`Claim filed successfully! Reference: ${created.referenceNumber}`, 'success');
        this.claimForm.reset({
          market: 'MY',
          claimType: 'MOTOR',
          policyNumber: '',
          claimantName: 'Nurul Ain',
          incidentDate: new Date().toISOString().substring(0, 10),
          description: '',
          estimatedAmount: 1500.00
        });
        this.loadClaims();
      },
      error: (err) => {
        this.submitting = false;
        this.showAlert('Submission failed: ' + (err.error?.message || err.message), 'danger');
      }
    });
  }

  public openDetailModal(id: string): void {
    this.claimService.getClaimById(id).subscribe({
      next: (detail) => {
        this.selectedClaim = detail;
        this.provideInfoNote = '';
      },
      error: (err) => {
        this.showAlert('Could not load claim details: ' + (err.error?.message || err.message), 'danger');
      }
    });
  }

  public closeDetailModal(): void {
    this.selectedClaim = null;
    this.provideInfoNote = '';
  }

  public submitProvidedInfo(): void {
    if (!this.selectedClaim || !this.provideInfoNote.trim()) return;

    this.submittingInfo = true;
    this.assessmentService.provideInfo(this.selectedClaim.id, { note: this.provideInfoNote }).subscribe({
      next: (updated) => {
        this.submittingInfo = false;
        this.selectedClaim = updated;
        this.provideInfoNote = '';
        this.showAlert(`Information provided for ${updated.referenceNumber}. Review resumed.`, 'success');
        this.loadClaims();
      },
      error: (err) => {
        this.submittingInfo = false;
        this.showAlert('Failed to submit info: ' + (err.error?.message || err.message), 'danger');
      }
    });
  }

  private showAlert(msg: string, type: 'success' | 'danger' | 'warning'): void {
    this.alertMessage = msg;
    this.alertType = type;
    setTimeout(() => {
      if (this.alertMessage === msg) this.alertMessage = null;
    }, 6000);
  }
}
