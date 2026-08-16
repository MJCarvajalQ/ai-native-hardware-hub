import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function AppShell() {
  const { logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <aside style={{ width: 220, borderRight: '1px solid #ddd', padding: '1rem' }}>
        <h2>Hardware Manager</h2>
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <NavLink to="/">Hardware List</NavLink>
          <NavLink to="/my-rentals">My Rentals</NavLink>
          <NavLink to="/admin">Admin Panel</NavLink>
        </nav>
        <button type="button" onClick={handleLogout} style={{ marginTop: '2rem' }}>
          Log out
        </button>
      </aside>
      <main style={{ flex: 1, padding: '1.5rem' }}>
        <Outlet />
      </main>
    </div>
  )
}
