import {
    createContext,
    useContext,
    useEffect,
    useState,
    type ReactNode,
  } from "react";
  
  import api from "../services/api";
  import type { LoginRequest, LoginResponse } from "../types/auth";
  
  interface AuthContextType {
    user: LoginResponse | null;
    token: string | null;
    login: (credentials: LoginRequest) => Promise<void>;
    logout: () => void;
    isAuthenticated: boolean;
  }
  
  const AuthContext = createContext<AuthContextType | undefined>(undefined);
  
  interface AuthProviderProps {
    children: ReactNode;
  }
  
  export function AuthProvider({ children }: AuthProviderProps) {
    const [user, setUser] = useState<LoginResponse | null>(null);
    const [token, setToken] = useState<string | null>(
      localStorage.getItem("token")
    );
  
    useEffect(() => {
      const storedUser = localStorage.getItem("user");
  
      if (storedUser) {
        try {
          setUser(JSON.parse(storedUser));
        } catch {
          localStorage.removeItem("user");
        }
      }
    }, []);
  
    const login = async (credentials: LoginRequest) => {
      const response = await api.post<LoginResponse>(
        "/auth/login",
        credentials
      );
  
      const loginData = response.data;
  
      localStorage.setItem("token", loginData.token);
      localStorage.setItem("user", JSON.stringify(loginData));
  
      setToken(loginData.token);
      setUser(loginData);
    };
  
    const logout = () => {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
  
      setToken(null);
      setUser(null);
    };
  
    return (
      <AuthContext.Provider
        value={{
          user,
          token,
          login,
          logout,
          isAuthenticated: Boolean(token),
        }}
      >
        {children}
      </AuthContext.Provider>
    );
  }
  
  export function useAuth(): AuthContextType {
    const context = useContext(AuthContext);
  
    if (!context) {
      throw new Error("useAuth must be used inside AuthProvider");
    }
  
    return context;
  }