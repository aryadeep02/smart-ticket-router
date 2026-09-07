import api from "./api";
import type {
  CreateCommentRequest,
  TicketComment,
} from "../types/comment";

export async function getComments(
  ticketId: number
): Promise<TicketComment[]> {
  const response = await api.get<TicketComment[]>(
    `/tickets/${ticketId}/comments`
  );

  return response.data;
}

export async function addComment(
  ticketId: number,
  request: CreateCommentRequest
): Promise<TicketComment> {
  const response = await api.post<TicketComment>(
    `/tickets/${ticketId}/comments`,
    request
  );

  return response.data;
}