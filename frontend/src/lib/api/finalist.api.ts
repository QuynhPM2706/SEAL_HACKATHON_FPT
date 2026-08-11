import { api } from "./api-client";

export type FinalistSelectionMethod =
  | "TOP_PER_TRACK"
  | "TOP_PER_GROUP"
  | "OVERFLOW_FILL"
  | "PENALTY_PENDING"
  | "MANUAL";
export type ContestedSlotType = "PER_TRACK_CUTOFF" | "PER_GROUP_CUTOFF" | "OVERFLOW_FILL";
export type FinalistSelectionMode = "AUTO" | "MANUAL";

export interface FinalistResponse {
  id: string;
  eventId: string;
  teamId: string;
  teamName: string;
  trackId: string | null;
  trackName: string | null;
  preliminaryRank: number;
  selectedReason: string | null;
  selectedAt: string;
  selectionMethod?: FinalistSelectionMethod | null;
  needsPenaltyEvaluation?: boolean;
}

export interface ContestedTeamResponse {
  teamId: string;
  teamName: string;
  finalScore: number;
  submittedAt: string | null;
}

export interface ContestedSlotResponse {
  id: string;
  trackId: string | null;
  trackName: string | null;
  slotType: ContestedSlotType;
  slotIndex: number;
  needsPenaltyEvaluation: boolean;
  teams: ContestedTeamResponse[];
}

export interface FinalistSelectionSummaryResponse {
  selectedCount: number;
  targetCount: number;
  penaltyEvaluationRequired: boolean;
  bucketScope?: string | null;
  bucketLabel?: string | null;
  topN?: number | null;
}

export interface FinalistSelectResultResponse {
  finalists: FinalistResponse[];
  contestedSlots: ContestedSlotResponse[];
  summary: FinalistSelectionSummaryResponse;
}

export interface SelectFinalistsRequest {
  mode: FinalistSelectionMode;
  topN?: number;
  teamIds?: string[];
}

export const finalistApi = {
  preview(eventId: string, body: SelectFinalistsRequest): Promise<FinalistSelectResultResponse> {
    return api.post<FinalistSelectResultResponse>(`/events/${eventId}/finalists/preview`, body);
  },

  select(eventId: string, body?: SelectFinalistsRequest): Promise<FinalistSelectResultResponse> {
    return api.post<FinalistSelectResultResponse>(`/events/${eventId}/finalists/select`, body ?? {});
  },

  list(eventId: string): Promise<FinalistResponse[]> {
    return api.get<FinalistResponse[]>(`/events/${eventId}/finalists`);
  },

  listContested(eventId: string): Promise<ContestedSlotResponse[]> {
    return api.get<ContestedSlotResponse[]>(`/events/${eventId}/finalists/contested`);
  },
};
