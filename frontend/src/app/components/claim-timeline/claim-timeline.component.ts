import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClaimActivity } from '../../models/claim.model';

@Component({
  selector: 'app-claim-timeline',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="timeline-container">
      <h4 class="timeline-title">Audit Trail & Activity Timeline (Kafka Logged)</h4>
      <div *ngIf="activities.length === 0" class="empty-timeline">
        No activities recorded yet.
      </div>
      <div class="timeline">
        <div *ngFor="let act of activities; let isLast = last" class="timeline-item" [class.last-item]="isLast">
          <div class="timeline-marker" [ngClass]="getMarkerClass(act.actionType)"></div>
          <div class="timeline-content">
            <div class="timeline-header">
              <span class="action-badge" [ngClass]="getActionClass(act.actionType)">
                {{ formatActionType(act.actionType) }}
              </span>
              <span class="actor-badge" [ngClass]="'role-' + act.actorRole.toLowerCase()">
                {{ act.actorRole }}: {{ act.actorId }}
              </span>
              <span class="timeline-time">{{ act.createdAt | date:'medium' }}</span>
            </div>
            <div *ngIf="act.note" class="timeline-note">
              "{{ act.note }}"
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .timeline-container {
      margin-top: 1rem;
    }
    .timeline-title {
      font-size: 0.95rem;
      font-weight: 600;
      color: #1e1035;
      margin-bottom: 1rem;
    }
    .empty-timeline {
      font-size: 0.85rem;
      color: #64748b;
      padding: 1rem 0;
    }
    .timeline {
      position: relative;
      padding-left: 1.5rem;
    }
    .timeline::before {
      content: '';
      position: absolute;
      left: 7px;
      top: 5px;
      bottom: 5px;
      width: 2px;
      background: #e2e8f0;
    }
    .timeline-item {
      position: relative;
      margin-bottom: 1.25rem;
    }
    .timeline-item.last-item {
      margin-bottom: 0;
    }
    .timeline-marker {
      position: absolute;
      left: -1.5rem;
      top: 4px;
      width: 16px;
      height: 16px;
      border-radius: 50%;
      background: white;
      border: 3px solid #94a3b8;
      z-index: 1;
    }
    .timeline-marker.marker-submit { border-color: #3b82f6; }
    .timeline-marker.marker-pickup { border-color: #6366f1; }
    .timeline-marker.marker-request { border-color: #f59e0b; }
    .timeline-marker.marker-provide { border-color: #06b6d4; }
    .timeline-marker.marker-settle { border-color: #10b981; }
    .timeline-marker.marker-reject { border-color: #ef4444; }

    .timeline-content {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      padding: 0.75rem 1rem;
    }
    .timeline-header {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.775rem;
    }
    .action-badge {
      font-weight: 700;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      font-size: 0.7rem;
    }
    .action-submitted { background: #dbeafe; color: #1e40af; }
    .action-picked_up { background: #e0e7ff; color: #3730a3; }
    .action-info_requested { background: #fef3c7; color: #92400e; }
    .action-info_provided { background: #cffafe; color: #155e75; }
    .action-settled { background: #d1fae5; color: #065f46; }
    .action-rejected { background: #fee2e2; color: #991b1b; }

    .actor-badge {
      padding: 0.15rem 0.4rem;
      border-radius: 4px;
      font-size: 0.7rem;
      font-weight: 500;
    }
    .role-claimant { background: #f1f5f9; color: #334155; }
    .role-officer { background: #ede9fe; color: #5b21b6; }
    .role-system { background: #e2e8f0; color: #475569; }

    .timeline-time {
      margin-left: auto;
      color: #94a3b8;
      font-size: 0.7rem;
    }
    .timeline-note {
      margin-top: 0.4rem;
      font-size: 0.8rem;
      color: #334155;
      background: white;
      padding: 0.4rem 0.6rem;
      border-radius: 4px;
      border-left: 3px solid #a3238e;
      font-style: italic;
    }
  `]
})
export class ClaimTimelineComponent {
  @Input() activities: ClaimActivity[] = [];

  public formatActionType(action: string): string {
    return action.replace('_', ' ');
  }

  public getMarkerClass(action: string): string {
    switch (action) {
      case 'SUBMITTED': return 'marker-submit';
      case 'PICKED_UP': return 'marker-pickup';
      case 'INFO_REQUESTED': return 'marker-request';
      case 'INFO_PROVIDED': return 'marker-provide';
      case 'SETTLED': return 'marker-settle';
      case 'REJECTED': return 'marker-reject';
      default: return '';
    }
  }

  public getActionClass(action: string): string {
    return 'action-' + action.toLowerCase();
  }
}
