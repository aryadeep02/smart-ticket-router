import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import CreateTicket from "./pages/CreateTicket";
import TicketDetail from "./pages/TicketDetail";
import AdminUsers from "./pages/AdminUsers";
import AdminTeams from "./pages/AdminTeams";
import OAuth2Callback from "./pages/OAuth2Callback";
import VerifyEmail from "./pages/VerifyEmail";
import ProtectedRoute from "./components/ProtectedRoute";

function App() {
  return (
    <BrowserRouter>
    <Routes>

        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route
            path="/verify-email"
            element={<VerifyEmail />}
        />

        <Route
            path="/oauth2/callback"
            element={<OAuth2Callback />}
        />

        <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route
                path="/tickets/create"
                element={<CreateTicket />}
            />
            <Route
                path="/tickets/:id"
                element={<TicketDetail />}
            />
            <Route
                path="/admin/users"
                element={<AdminUsers />}
            />
            <Route
                path="/admin/teams"
                element={<AdminTeams />}
            />
        </Route>

        <Route
            path="*"
            element={<Navigate to="/login" replace />}
        />

    </Routes>
</BrowserRouter>
  );
}

export default App;