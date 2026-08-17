import { useCallback, useEffect, useState } from 'react'
import { listHardware, rentHardware, returnHardware } from '../api/hardware'
import { searchHardware } from '../api/search'
import { useAuth } from '../auth/AuthContext'
import { StatusPill } from '../components/StatusPill'

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

  // AI search state. searchResults is null when no AI search is active (the
  // normal sorted/filtered table renders instead); an array — possibly
  // empty — once a search has run. Each entry is {hardware, reason}.
  const [searchQuery, setSearchQuery] = useState('')
  const [searchResults, setSearchResults] = useState(null)
  const [searchLoading, setSearchLoading] = useState(false)
  const [searchError, setSearchError] = useState(null)

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

  async function handleSearchSubmit(event) {
    event.preventDefault()
    const trimmed = searchQuery.trim()
    if (!trimmed) {
      setSearchResults(null)
      setSearchError(null)
      return
    }
    setSearchLoading(true)
    setSearchError(null)
    try {
      const results = await searchHardware(trimmed)
      setSearchResults(results)
    } catch (err) {
      setSearchError(err.message)
      setSearchResults(null)
    } finally {
      setSearchLoading(false)
    }
  }

  function handleSearchQueryChange(value) {
    setSearchQuery(value)
    if (!value.trim()) {
      // empty query restores the full list immediately, no need to submit
      setSearchResults(null)
      setSearchError(null)
    }
  }

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

  function renderAction(hardware) {
    const item = hardware
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

  const showingSearchResults = searchResults !== null

  return (
    <div>
      <h1>Hardware List</h1>

      <form onSubmit={handleSearchSubmit}>
        <input
          placeholder="Ask AI…"
          value={searchQuery}
          onChange={(e) => handleSearchQueryChange(e.target.value)}
        />
        <button type="submit" disabled={searchLoading}>
          {searchLoading ? 'Asking…' : 'Ask AI'}
        </button>
      </form>
      {searchError && (
        <p role="alert">Search is unavailable right now — try again, or use the filters below.</p>
      )}

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

      {showingSearchResults && searchResults.length === 0 && (
        <p>No matches for "{searchQuery.trim()}".</p>
      )}

      {!loading && !error && (showingSearchResults ? searchResults.length > 0 : true) && (
        <table>
          <thead>
            <tr>
              {COLUMNS.map((column) => (
                <th key={column.key}>
                  {showingSearchResults ? (
                    column.label
                  ) : (
                    <button type="button" onClick={() => handleSort(column.key)}>
                      {column.label}
                      {sortBy === column.key ? (direction === 'asc' ? ' ▲' : ' ▼') : ''}
                    </button>
                  )}
                </th>
              ))}
              {showingSearchResults && <th>Why</th>}
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {showingSearchResults
              ? searchResults.map(({ hardware, reason }) => (
                  <tr key={hardware.id}>
                    <td>{hardware.name}</td>
                    <td>{hardware.brand}</td>
                    <td>{formatDate(hardware.purchaseDate)}</td>
                    <td className="status-cell"><StatusPill status={hardware.status} /></td>
                    <td>{reason}</td>
                    <td className="action-cell">{renderAction(hardware)}</td>
                  </tr>
                ))
              : items.map((item) => (
                  <tr key={item.id}>
                    <td>{item.name}</td>
                    <td>{item.brand}</td>
                    <td>{formatDate(item.purchaseDate)}</td>
                    <td className="status-cell"><StatusPill status={item.status} /></td>
                    <td className="action-cell">{renderAction(item)}</td>
                  </tr>
                ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
