export type Market = 'MY' | 'SG' | 'TH' | 'HK' | 'AU' | 'JP';
export type ClaimType = 'MOTOR' | 'PROPERTY';
export type ClaimStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'PENDING_INFO' | 'SETTLED' | 'REJECTED';
export type UserRole = 'CLAIMANT' | 'OFFICER' | 'SYSTEM';
export type ActionType = 'SUBMITTED' | 'PICKED_UP' | 'INFO_REQUESTED' | 'INFO_PROVIDED' | 'SETTLED' | 'REJECTED';

export interface Claim {
  id: string;
  referenceNumber: string;
  market: Market;
  claimType: ClaimType;
  policyNumber: string;
  claimantName: string;
  claimantId: string;
  incidentDate: string;
  description: string;
  estimatedAmount: number;
  settledAmount?: number | null;
  status: ClaimStatus;
  assignedOfficerId?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ClaimActivity {
  id: string;
  claimId: string;
  actorId: string;
  actorRole: UserRole;
  actionType: ActionType;
  note?: string | null;
  createdAt: string;
}

export interface ClaimDetail extends Claim {
  activities: ClaimActivity[];
}

export interface MarketExposure {
  market: Market;
  openClaimCount: number;
  totalEstimatedExposure: number;
  totalSettledPayout: number;
  lastUpdatedAt: string;
}

export interface SubmitClaimRequest {
  market: Market;
  claimType: ClaimType;
  policyNumber: string;
  claimantName: string;
  incidentDate: string;
  description: string;
  estimatedAmount: number;
}

export interface RequestInfoRequest {
  note: string;
}

export interface ProvideInfoRequest {
  note: string;
}

export interface SettleClaimRequest {
  settledAmount: number;
  note?: string;
}

export interface RejectClaimRequest {
  rejectionReason: string;
}

export interface NotificationRecord {
  id: string;
  claimId: string;
  referenceNumber: string;
  recipient: string;
  title: string;
  message: string;
  channel: string;
  sentAt: string;
}
