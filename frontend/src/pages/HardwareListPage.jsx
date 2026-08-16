import { useCallback, useEffect, useState } from 'react'
import { listHardware, rentHardware, returnHardware } from '../api/hardware'
import { useAuth } from '../auth/AuthContext'

function formatDate(isoDate) {
  return isoDate ?? '—'
}

const COLUMNS = [
  { key: 'name', label: 'Name' },
  { key: 'brand', label: 'Brand' },
  { key: 'purchaseDate', label: 'Purchase Date' },
  { key: 'status', label: 'Status' },
]

const STATUSES = ['AVAILABLE', 'IN_USE', 'REPAIR']

export function HardwareListPage() {
  const { user } = useAuth()
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [actionError, setActionError] = useState(null)
  const [pendingId, setPendingId] = useState(null)
  const [sortBy, setSortBy] = useState('name')
  const [direction, setDirection] = useState('asc')
  const [statusFilter, setStatusFilter] = useState('')
  const [brandFilter, setBrandFilter] = useState('')
  const [availableBrands, setAvailableBrands] = useState([])

  // fetched once, unfiltered, purely to populate the brand dropdown options
  useEffect(() => {
    listHardware()
      .then((data) => {
        const brands = [...new Set(data.map((item) => item.brand).filter(Boolean))].sort()
        setAvailableBrands(brands)
      })
      .catch(() => {
        // non-critical: the dropdown just stays empty, the table's own
        // fetch below will surface the real error to the user
      })
  }, [])

  const refresh = useCallback(() => {
    setLoading(true)
    return listHardware({
      sortBy,
      direction,
      status: statusFilter || undefined,
      brand: brandFilter || undefined,
    })
      .then((data) => setItems(data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [sortBy, direction, statusFilter, brandFilter])

  useEffect(() => {
    refresh()
  }, [refresh])

  function handleSort(columnKey) {
    if (columnKey === sortBy) {
      setDirection((current) => (current === 'asc' ? 'desc' : 'asc'))
    } else {
      setSortBy(columnKey)
      setDirection('asc')
    }
  }

  async function handleRent(id) {
    setActionError(null)
    setPendingId(id)
    try {
      await rentHardware(id)
      await refresh()
    } catch (err) {
      setActionError(err.message)
    } finally {
      setPendingId(null)
    }
  }

  async function handleReturn(id) {
    setActionError(null)
    setPendingId(id)
    try {
      await returnHardware(id)
      await refresh()
    } catch (err) {
      setActionError(err.message)
    } finally {
      setPendingId(null)
    }
  }

  function renderAction(item) {
    const busy = pendingId === item.id
    if (item.status === 'AVAILABLE') {
      return (
        <button type="button" disabled={busy} onClick={() => handleRent(item.id)}>
          {busy ? 'Renting…' : 'Rent'}
        </button>
      )
    }
    if (item.status === 'IN_USE') {
      if (item.assignedTo === user?.email) {
        return (
          <button type="button" disabled={busy} onClick={() => handleReturn(item.id)}>
            {busy ? 'Returning…' : 'Return'}
          </button>
        )
      }
      return (
        <button type="button" disabled>
          In Use
        </button>
      )
    }
    return (
      <button type="button" disabled>
        In Repair
      </button>
    )
  }

  return (
    <div>
      <h1>Hardware List</h1>

      <div>
        <label>
          Status:{' '}
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="">All</option>
            {STATUSES.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </label>{' '}
        <label>
          Brand:{' '}
          <select value={brandFilter} onChange={(e) => setBrandFilter(e.target.value)}>
            <option value="">All</option>
            {availableBrands.map((brand) => (
              <option key={brand} value={brand}>
                {brand}
              </option>
            ))}
          </select>
        </label>
      </div>

      {actionError && <p role="alert">{actionError}</p>}
      {loading && <p>Loading…</p>}
      {error && <p role="alert">Failed to load hardware: {error}</p>}

      {!loading && !error && (
        <table>
          <thead>
            <tr>
              {COLUMNS.map((column) => (
                <th key={column.key}>
                  <button type="button" onClick={() => handleSort(column.key)}>
                    {column.label}
                    {sortBy === column.key ? (direction === 'asc' ? ' ▲' : ' ▼') : ''}
                  </button>
                </th>
              ))}
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                <td>{item.name}</td>
                <td>{item.brand}</td>
                <td>{formatDate(item.purchaseDate)}</td>
                <td>{item.status}</td>
                <td>{renderAction(item)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
