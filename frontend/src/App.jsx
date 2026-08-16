import { Routes, Route } from 'react-router-dom'
import { LoginPage } from './pages/LoginPage'
import { HardwareListPage } from './pages/HardwareListPage'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { AppShell } from './layout/AppShell'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route path="/" element={<HardwareListPage />} />
          <Route path="/my-rentals" element={<p>My rentals — built in Block I</p>} />
          <Route path="/admin" element={<p>Admin panel — built in Block J</p>} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App
