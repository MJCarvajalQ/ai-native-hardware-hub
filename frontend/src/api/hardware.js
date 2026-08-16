import { api } from './client'

export function listHardware({ sortBy, direction, status, brand } = {}) {
  const params = new URLSearchParams()
  if (sortBy) params.set('sortBy', sortBy)
  if (direction) params.set('direction', direction)
  if (status) params.set('status', status)
  if (brand) params.set('brand', brand)
  const query = params.toString()
  return api.get(`/api/hardware${query ? `?${query}` : ''}`)
}

export function createHardware(payload) {
  return api.post('/api/hardware', payload)
}

export function deleteHardware(id) {
  return api.delete(`/api/hardware/${id}`)
}

// no body: the acting user comes from the authenticated token, not
// anything the client sends (see PROMPTS.md for why)
export function rentHardware(id) {
  return api.post(`/api/hardware/${id}/rent`)
}

export function returnHardware(id) {
  return api.post(`/api/hardware/${id}/return`)
}

export function toggleRepair(id) {
  return api.patch(`/api/hardware/${id}/repair`)
}
