import axios from 'axios'

// Runtime config: check localStorage first, then environment variable
const getBaseURL = () => {
  // Runtime override from localStorage (for configurable server URL)
  const runtimeUrl = localStorage.getItem('server_url')
  if (runtimeUrl) return runtimeUrl

  // Build-time environment variable
  const envUrl = import.meta.env.VITE_API_URL
  return envUrl || ''
}

const api = axios.create({
  baseURL: getBaseURL(),
  timeout: 15000,
})

// Helper to update server URL at runtime
export const setServerUrl = (url: string) => {
  localStorage.setItem('server_url', url)
  api.defaults.baseURL = url
}

export const getServerUrl = () => {
  return localStorage.getItem('server_url') || import.meta.env.VITE_API_URL || ''
}

export default api