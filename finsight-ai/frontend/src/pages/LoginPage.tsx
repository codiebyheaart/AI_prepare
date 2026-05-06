import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../api/client'
import { TrendingUp, Mail, Lock, User, Eye, EyeOff } from 'lucide-react'

export default function LoginPage() {
  const navigate = useNavigate()
  const [isRegister, setIsRegister] = useState(false)
  const [showPass, setShowPass] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [form, setForm] = useState({ email: 'demo@finsight.ai', password: 'Demo@1234', fullName: '' })

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true); setError('')
    try {
      const res = isRegister
        ? await authApi.register(form.email, form.password, form.fullName)
        : await authApi.login(form.email, form.password)
      const { token, user } = res.data.data
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
      navigate('/dashboard')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Something went wrong')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-dark-900 flex items-center justify-center p-4">
      {/* Background glow */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/2 -translate-x-1/2 w-[600px] h-[600px] bg-brand-600/10 rounded-full blur-3xl" />
      </div>

      <div className="w-full max-w-md relative z-10 animate-fade-in">
        {/* Logo */}
        <div className="flex items-center justify-center gap-3 mb-8">
          <div className="w-12 h-12 bg-brand-600 rounded-2xl flex items-center justify-center shadow-lg shadow-brand-600/30">
            <TrendingUp size={22} className="text-white" />
          </div>
          <div>
            <div className="text-2xl font-bold text-white">FinSight AI</div>
            <div className="text-xs text-slate-500">AI-Powered Trading Platform</div>
          </div>
        </div>

        <div className="card">
          <h1 className="text-xl font-bold text-white mb-1">
            {isRegister ? 'Create Account' : 'Welcome back'}
          </h1>
          <p className="text-sm text-slate-400 mb-6">
            {isRegister ? 'Start your AI trading journey' : 'Sign in to your account'}
          </p>

          {/* Demo credentials hint */}
          {!isRegister && (
            <div className="bg-brand-600/10 border border-brand-600/20 rounded-xl p-3 mb-5 text-xs text-brand-400">
              🎯 Demo: <span className="font-mono">demo@finsight.ai</span> / <span className="font-mono">Demo@1234</span>
            </div>
          )}

          <form onSubmit={submit} className="space-y-4">
            {isRegister && (
              <div className="relative">
                <User size={16} className="absolute left-3.5 top-3.5 text-slate-500" />
                <input className="input pl-10" placeholder="Full name" required
                  value={form.fullName} onChange={e => setForm(f => ({ ...f, fullName: e.target.value }))} />
              </div>
            )}
            <div className="relative">
              <Mail size={16} className="absolute left-3.5 top-3.5 text-slate-500" />
              <input className="input pl-10" type="email" placeholder="Email address" required
                value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))} />
            </div>
            <div className="relative">
              <Lock size={16} className="absolute left-3.5 top-3.5 text-slate-500" />
              <input className="input pl-10 pr-10" type={showPass ? 'text' : 'password'} placeholder="Password" required
                value={form.password} onChange={e => setForm(f => ({ ...f, password: e.target.value }))} />
              <button type="button" onClick={() => setShowPass(s => !s)}
                className="absolute right-3.5 top-3.5 text-slate-500 hover:text-white transition-colors">
                {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>

            {error && <p className="text-danger text-sm bg-danger/10 rounded-xl p-3">{error}</p>}

            <button type="submit" disabled={loading} className="btn-primary w-full">
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  {isRegister ? 'Creating account...' : 'Signing in...'}
                </span>
              ) : isRegister ? 'Create Account' : 'Sign In'}
            </button>
          </form>

          <p className="text-center text-sm text-slate-400 mt-5">
            {isRegister ? 'Already have an account?' : "Don't have an account?"}
            {' '}
            <button onClick={() => { setIsRegister(r => !r); setError('') }}
              className="text-brand-400 hover:text-brand-300 font-medium transition-colors">
              {isRegister ? 'Sign in' : 'Create one'}
            </button>
          </p>
        </div>

        <p className="text-center text-xs text-slate-600 mt-4">
          Built with Spring Boot 3 · Spring AI · RAG · pgvector
        </p>
      </div>
    </div>
  )
}
