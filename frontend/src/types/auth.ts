export type UserRole = "CUSTOMER" | "AGENT" | "ADMIN";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: UserRole;
}