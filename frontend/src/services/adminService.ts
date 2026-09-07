import api from "./api";

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  role: "CUSTOMER" | "AGENT" | "ADMIN";
  supportTeamId: number | null;
  supportTeamName: string | null;
}

export interface SupportTeam {
  id: number;
  name: string;
  description: string;
}

export async function getAdminUsers(): Promise<AdminUser[]> {
  const response = await api.get<AdminUser[]>(
    "/admin/users"
  );

  return response.data;
}

export async function getAdminTeams(): Promise<SupportTeam[]> {
  const response = await api.get<SupportTeam[]>(
    "/admin/teams"
  );

  return response.data;
}
export async function getAgents(): Promise<AdminUser[]> {
    const response = await api.get<AdminUser[]>(
      "/admin/users"
    );
  
    return response.data.filter(
      (user) => user.role === "AGENT"
    );
  }
  export interface CreateUserRequest {
    name: string;
    email: string;
    password: string;
    role: "CUSTOMER" | "AGENT" | "ADMIN";
    supportTeamId?: number;
  }
  
  export interface CreateUserResponse {
    id: number;
    name: string;
    email: string;
    role: string;
  }
  
  export async function createAdminUser(
    request: CreateUserRequest
  ): Promise<CreateUserResponse> {
    const response = await api.post<CreateUserResponse>(
      "/admin/users",
      request
    );
  
    return response.data;
  }