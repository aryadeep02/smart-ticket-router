import api from "./api";
import type { Ticket } from "../types/ticket";

/* =========================================================
   TICKET FILTERS
========================================================= */

export interface TicketFilters {
  status?: string;
  priority?: string;
  category?: string;
  slaBreached?: boolean;
}

/* =========================================================
   PAGINATED TICKET RESPONSE
========================================================= */

export interface TicketPage {
  content: Ticket[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/* =========================================================
   GET TICKETS
========================================================= */

export async function getTickets(
  filters?: TicketFilters,
  page: number = 0,
  size: number = 10
): Promise<TicketPage> {
  const response = await api.get<TicketPage>("/tickets", {
    params: {
      ...filters,
      page,
      size,
    },
  });

  return response.data;
}

/* =========================================================
   GET SINGLE TICKET
========================================================= */

export async function getTicket(
  ticketId: number
): Promise<Ticket> {
  const response = await api.get<Ticket>(
    `/tickets/${ticketId}`
  );

  return response.data;
}

/* =========================================================
   UPDATE TICKET STATUS
========================================================= */

export interface UpdateTicketStatusRequest {
  status: string;
}

export async function updateTicketStatus(
  ticketId: number,
  status: string
): Promise<Ticket> {
  const response = await api.patch<Ticket>(
    `/tickets/${ticketId}/status`,
    {
      status,
    }
  );

  return response.data;
}

/* =========================================================
   CREATE TICKET
========================================================= */

export interface CreateTicketRequest {
  title: string;
  description: string;
}

export async function createTicket(
  request: CreateTicketRequest
): Promise<Ticket> {
  const response = await api.post<Ticket>(
    "/tickets",
    request
  );

  return response.data;
}

/* =========================================================
   ASSIGN AGENT
========================================================= */

export interface AssignAgentRequest {
  agentId: number;
}

export async function assignAgent(
  ticketId: number,
  agentId: number
): Promise<Ticket> {
  const response = await api.patch<Ticket>(
    `/tickets/${ticketId}/assign`,
    {
      agentId,
    }
  );

  return response.data;
}

/* =========================================================
   ADMIN DASHBOARD SUMMARY
========================================================= */

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
  const response =
    await api.get<AdminDashboardSummary>(
      "/admin/dashboard"
    );

  return response.data;
}