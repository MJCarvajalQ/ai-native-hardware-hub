import { Routes, Route } from 'react-router-dom'
import { LoginPage } from './pages/LoginPage'
import { HardwareListPage } from './pages/HardwareListPage'
import { AdminPage } from './pages/AdminPage'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { AdminRoute } from './auth/AdminRoute'
import { AppShell } from './layout/AppShell'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route path="/" element={<HardwareListPage />} />
          <Route path="/my-rentals" element={<p>My Rentals — a Part 2 increment, not required for the MVP</p>} />
          <Route element={<AdminRoute />}>
            <Route path="/admin" element={<AdminPage />} />
          </Route>
        </Route>
      </Route>
    </Routes>
  )
}

export default App
