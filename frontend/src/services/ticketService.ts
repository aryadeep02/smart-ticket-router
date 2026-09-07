import api from "./api";
import type { Ticket } from "../types/ticket";

export interface TicketFilters {
  status?: string;
  priority?: string;
  category?: string;
  slaBreached?: boolean;
}

export async function getTickets(filters?: TicketFilters): Promise<Ticket[]> {
  const response = await api.get<Ticket[]>("/tickets", {
    params: filters,
  });

  return response.data;
}

export async function getTicket(ticketId: number): Promise<Ticket> {
  const response = await api.get<Ticket>(`/tickets/${ticketId}`);

  return response.data;
}

export interface UpdateTicketStatusRequest {
  status: string;
}

export async function updateTicketStatus(
  ticketId: number,
  status: string
): Promise<Ticket> {
  const response = await api.patch<Ticket>(`/tickets/${ticketId}/status`, {
    status,
  });

  return response.data;
}
export interface CreateTicketRequest {
  title: string;
  description: string;
}

export async function createTicket(
  request: CreateTicketRequest
): Promise<Ticket> {
  const response = await api.post<Ticket>("/tickets", request);

  return response.data;
}
export interface AssignAgentRequest {
  agentId: number;
}

export async function assignAgent(
  ticketId: number,
  agentId: number
): Promise<Ticket> {
  const response = await api.patch<Ticket>(`/tickets/${ticketId}/assign`, {
    agentId,
  });

  return response.data;
}
export interface AdminDashboardSummary {
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  resolvedTickets: number;
  breachedTickets: number;
  unassignedTickets: number;
  criticalTickets: number;
}

export async function getAdminDashboardSummary(): Promise<AdminDashboardSummary> {
  const response = await api.get<AdminDashboardSummary>(
    "/tickets/admin/summary"
  );
  return response.data;
}
