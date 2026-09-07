export type TicketCategory =
  | "ACCOUNT"
  | "PAYMENT"
  | "TECHNICAL"
  | "DELIVERY"
  | "REFUND"
  | "OTHER";

export type TicketPriority =
  | "LOW"
  | "MEDIUM"
  | "HIGH"
  | "CRITICAL";

export type TicketStatus =
  | "OPEN"
  | "IN_PROGRESS"
  | "WAITING_FOR_CUSTOMER"
  | "RESOLVED"
  | "CLOSED";

export interface Ticket {
  id: number;
  title: string;
  description: string;

  category: TicketCategory;
  priority: TicketPriority;
  status: TicketStatus;

  customerId: number;
  assignedAgentId: number | null;
  supportTeamId: number | null;

  aiConfidence: number | null;
  aiClassificationStatus: string;

  slaDeadline: string;
  slaBreached: boolean;

  resolvedAt: string | null;
  createdAt: string;
  updatedAt: string;
}