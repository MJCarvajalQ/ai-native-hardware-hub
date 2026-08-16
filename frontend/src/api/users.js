import { api } from './client'

export function createUser(payload) {
  return api.post('/api/users', payload)
}
