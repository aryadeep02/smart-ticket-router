export interface TicketComment {
    id: number;
    ticketId: number;
    authorId: number;
    authorName: string;
    content: string;
    createdAt: string;
  }
  
  export interface CreateCommentRequest {
    content: string;
  }