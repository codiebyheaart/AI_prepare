import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

// Attach JWT token to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Redirect to login on 401
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ── Auth ────────────────────────────────────────────────────────────
export const authApi = {
  login:    (email: string, password: string) => api.post('/auth/login', { email, password }),
  register: (email: string, password: string, fullName: string) =>
              api.post('/auth/register', { email, password, fullName }),
  me:       () => api.get('/auth/me'),
}

// ── Portfolio ───────────────────────────────────────────────────────
export const portfolioApi = {
  get:          () => api.get('/portfolio'),
  addHolding:   (data: object) => api.post('/portfolio/holdings', data),
  removeHolding:(id: number)   => api.delete(`/portfolio/holdings/${id}`),
  transactions: () => api.get('/portfolio/transactions'),
}

// ── Market ──────────────────────────────────────────────────────────
export const marketApi = {
  quote:    (symbol: string) => api.get(`/market/quote/${symbol}`),
  search:   (q: string)      => api.get('/market/search', { params: { q } }),
  trending: ()               => api.get('/market/trending'),
  indices:  ()               => api.get('/market/indices'),
}

// ── AI ──────────────────────────────────────────────────────────────
export const aiApi = {
  chat:          (sessionId: string | null, message: string) =>
                   api.post('/ai/chat', { sessionId, message }),
  chatHistory:   (sessionId: string) => api.get(`/ai/chat/history/${sessionId}`),
  clearChat:     (sessionId: string) => api.delete(`/ai/chat/history/${sessionId}`),
  research:      (symbol: string)    => api.post(`/ai/research/${symbol}`),
  researchHistory: ()                => api.get('/ai/research/history'),
  semanticSearch: (q: string, topK = 5) =>
                   api.get('/documents/semantic-search', { params: { q, topK } }),
}

// ── Documents ────────────────────────────────────────────────────────
export const docsApi = {
  list:   () => api.get('/documents'),
  upload: (file: File, docType = 'GENERAL') => {
    const form = new FormData()
    form.append('file', file)
    form.append('docType', docType)
    return api.post('/documents/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
}

export default api
