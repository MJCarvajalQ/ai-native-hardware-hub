import { api } from './client'

export function searchHardware(query) {
  return api.post('/api/search', { query })
}
