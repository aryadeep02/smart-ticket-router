import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";
import TicketDetail from "./pages/TicketDetail";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import ProtectedRoute from "./components/ProtectedRoute";
import CreateTicket from "./pages/CreateTicket";
import AdminUsers from "./pages/AdminUsers";
import AdminTeams from "./pages/AdminTeams";
import Register from "./pages/Register";
function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />

        <Route element={<ProtectedRoute />}>
  <Route path="/dashboard" element={<Dashboard />} />
  <Route path="/tickets/:id" element={<TicketDetail />} />
</Route>

        <Route
          path="*"
          element={<Navigate to="/login" replace />}
        />

<Route element={<ProtectedRoute />}>
  <Route path="/dashboard" element={<Dashboard />} />
  <Route path="/tickets/:id" element={<TicketDetail />} />
  <Route path="/tickets/create" element={<CreateTicket />} />
</Route>
<Route element={<ProtectedRoute />}>
  <Route path="/dashboard" element={<Dashboard />} />
  <Route path="/tickets/:id" element={<TicketDetail />} />
  <Route path="/tickets/create" element={<CreateTicket />} />
  <Route path="/admin/users" element={<AdminUsers />} />
</Route>
<Route
  path="/admin/teams"
  element={<AdminTeams />}
/>
<Route path="/register" element={<Register />} />
      </Routes>
    </BrowserRouter>
    
  );
}

export default App;