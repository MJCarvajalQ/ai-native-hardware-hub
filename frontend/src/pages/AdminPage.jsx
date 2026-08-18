import { useCallback, useEffect, useState } from 'react'
import { listHardware, createHardware, deleteHardware, toggleRepair } from '../api/hardware'
import { createUser } from '../api/users'
import { StatusPill } from '../components/StatusPill'

export function AdminPage() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const refresh = useCallback(() => {
    setLoading(true)
    return listHardware()
      .then(setItems)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    refresh()
  }, [refresh])

  // --- Add device form ---
  const [newName, setNewName] = useState('')
  const [newBrand, setNewBrand] = useState('')
  const [newDate, setNewDate] = useState('')
  const [newNotes, setNewNotes] = useState('')
  const [addError, setAddError] = useState(null)
  const [adding, setAdding] = useState(false)

  async function handleAdd(event) {
    event.preventDefault()
    setAddError(null)
    setAdding(true)
    try {
      await createHardware({
        name: newName,
        brand: newBrand,
        purchaseDate: newDate || null,
        notes: newNotes || null,
      })
      setNewName('')
      setNewBrand('')
      setNewDate('')
      setNewNotes('')
      await refresh()
    } catch (err) {
      setAddError(err.message)
    } finally {
      setAdding(false)
    }
  }

  async function handleDelete(id) {
    try {
      await deleteHardware(id)
      await refresh()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleToggleRepair(id) {
    try {
      await toggleRepair(id)
      await refresh()
    } catch (err) {
      setError(err.message)
    }
  }

  // --- Create user form ---
  const [userEmail, setUserEmail] = useState('')
  const [userPassword, setUserPassword] = useState('')
  const [userError, setUserError] = useState(null)
  const [userSuccess, setUserSuccess] = useState(null)
  const [creatingUser, setCreatingUser] = useState(false)

  async function handleCreateUser(event) {
    event.preventDefault()
    setUserError(null)
    setUserSuccess(null)
    setCreatingUser(true)
    try {
      const created = await createUser({ email: userEmail, password: userPassword })
      setUserSuccess(`Created account for ${created.email}`)
      setUserEmail('')
      setUserPassword('')
    } catch (err) {
      setUserError(err.message)
    } finally {
      setCreatingUser(false)
    }
  }

  return (
    <div>
      <h1>Hardware Management</h1>

      <section>
        <h2>Add New Device</h2>
        <form onSubmit={handleAdd}>
          <input
            placeholder="Name"
            value={newName}
            onChange={(event) => setNewName(event.target.value)}
            required
          />
          <input
            placeholder="Brand"
            value={newBrand}
            onChange={(event) => setNewBrand(event.target.value)}
            required
          />
          <input
            type="date"
            value={newDate}
            onChange={(event) => setNewDate(event.target.value)}
          />
          <input
            placeholder="Notes (optional)"
            value={newNotes}
            onChange={(event) => setNewNotes(event.target.value)}
          />
          <button type="submit" disabled={adding}>
            {adding ? 'Adding…' : '+ Add New Device'}
          </button>
        </form>
        {addError && <p role="alert">{addError}</p>}
      </section>

      <section>
        <h2>Create User</h2>
        <p>Account creation is the only way for someone to gain access to the Hub.</p>
        <form onSubmit={handleCreateUser}>
          <input
            type="email"
            placeholder="Email"
            value={userEmail}
            onChange={(event) => setUserEmail(event.target.value)}
            required
          />
          <input
            type="password"
            placeholder="Password"
            value={userPassword}
            onChange={(event) => setUserPassword(event.target.value)}
            required
          />
          <button type="submit" disabled={creatingUser}>
            {creatingUser ? 'Creating…' : 'Create User'}
          </button>
        </form>
        {userError && <p role="alert">{userError}</p>}
        {userSuccess && <p>{userSuccess}</p>}
      </section>

      <section>
        <h2>Hardware</h2>
        {loading && <p>Loading…</p>}
        {error && <p role="alert">{error}</p>}
        {!loading && !error && (
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Brand</th>
                <th>Purchase Date</th>
                <th>Status</th>
                <th>Actions</th>
                <th>Delete</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id}>
                  <td>{item.name}</td>
                  <td>{item.brand}</td>
                  <td>{item.purchaseDate ?? '—'}</td>
                  <td className="status-cell"><StatusPill status={item.status} /></td>
                  <td className="action-cell">
                    <button
                      type="button"
                      onClick={() => handleToggleRepair(item.id)}
                      disabled={item.status === 'IN_USE'}
                      title={item.status === 'IN_USE' ? 'Return it first' : undefined}
                    >
                      {item.status === 'REPAIR' ? 'Mark Available' : 'Send to Repair'}
                    </button>
                  </td>
                  <td className="action-cell">
                    <button
                      type="button"
                      className="button-danger"
                      onClick={() => handleDelete(item.id)}
                      disabled={item.status === 'IN_USE'}
                      title={item.status === 'IN_USE' ? 'Return it first' : undefined}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  )
}
