import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { LayoutDashboard, Briefcase, MessageSquare, Search, LogOut, TrendingUp } from 'lucide-react'

const nav = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/portfolio',  icon: Briefcase,       label: 'Portfolio' },
  { to: '/chat',       icon: MessageSquare,   label: 'AI Chat' },
  { to: '/research',   icon: Search,          label: 'Research' },
]

export default function Layout() {
  const navigate = useNavigate()
  const user = JSON.parse(localStorage.getItem('user') || '{}')

  function logout() {
    localStorage.clear()
    navigate('/login')
  }

  return (
    <div className="flex h-screen overflow-hidden">
      {/* Sidebar */}
      <aside className="w-64 bg-dark-card border-r border-dark-border flex flex-col shrink-0">
        {/* Logo */}
        <div className="flex items-center gap-3 px-6 py-5 border-b border-dark-border">
          <div className="w-9 h-9 bg-brand-600 rounded-xl flex items-center justify-center">
            <TrendingUp size={18} className="text-white" />
          </div>
          <div>
            <div className="font-bold text-white text-sm">FinSight AI</div>
            <div className="text-xs text-slate-500">Trading Intelligence</div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-3 py-4 space-y-1">
          {nav.map(({ to, icon: Icon, label }) => (
            <NavLink key={to} to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 ${
                  isActive
                    ? 'bg-brand-600/20 text-brand-400 border border-brand-600/30'
                    : 'text-slate-400 hover:text-white hover:bg-white/5'
                }`
              }>
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        {/* User area */}
        <div className="px-3 py-4 border-t border-dark-border">
          <div className="flex items-center gap-3 px-3 py-2 mb-2">
            <div className="w-8 h-8 bg-brand-600 rounded-full flex items-center justify-center text-xs font-bold">
              {user.fullName?.charAt(0) ?? 'U'}
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-sm font-medium text-white truncate">{user.fullName ?? 'User'}</div>
              <div className="text-xs text-slate-500 truncate">{user.email ?? ''}</div>
            </div>
          </div>
          <button onClick={logout}
            className="flex items-center gap-2 w-full px-4 py-2 rounded-xl text-sm text-slate-400 hover:text-danger hover:bg-danger/10 transition-all duration-200">
            <LogOut size={16} />
            Sign out
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-y-auto bg-dark-900">
        <Outlet />
      </main>
    </div>
  )
}
