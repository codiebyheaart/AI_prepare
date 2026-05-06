import { useEffect, useState } from 'react'
import { marketApi } from '../api/client'
import { TrendingUp, TrendingDown, Activity, ArrowUpRight } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

interface Quote { symbol: string; companyName: string; price: number; change: number; changePercent: number; volume: number }
interface Indices { NIFTY50?: Quote; SENSEX?: Quote; BANKNIFTY?: Quote }

export default function DashboardPage() {
  const [indices, setIndices]   = useState<Indices>({})
  const [trending, setTrending] = useState<Quote[]>([])
  const [loading, setLoading]   = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    async function load() {
      try {
        const [idx, trend] = await Promise.all([marketApi.indices(), marketApi.trending()])
        setIndices(idx.data.data)
        setTrending(trend.data.data)
      } catch (e) { console.error(e) }
      finally { setLoading(false) }
    }
    load()
    const interval = setInterval(load, 15000)
    return () => clearInterval(interval)
  }, [])

  function fmt(n: number) { return n?.toLocaleString('en-IN', { maximumFractionDigits: 2 }) ?? '-' }
  function pct(n: number) { return (n > 0 ? '+' : '') + n?.toFixed(2) + '%' }
  function isUp(n: number) { return n >= 0 }

  if (loading) return (
    <div className="flex items-center justify-center h-full">
      <div className="w-8 h-8 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" />
    </div>
  )

  const indexList = [
    { key: 'NIFTY50',   label: 'Nifty 50' },
    { key: 'SENSEX',    label: 'BSE Sensex' },
    { key: 'BANKNIFTY', label: 'Bank Nifty' },
  ] as const

  return (
    <div className="p-8 space-y-8 animate-fade-in">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-white">Market Overview</h1>
        <p className="text-slate-400 mt-1 text-sm">Live market data · updates every 15s</p>
      </div>

      {/* Market Indices */}
      <div className="grid grid-cols-3 gap-4">
        {indexList.map(({ key, label }) => {
          const q = (indices as any)[key] as Quote | undefined
          if (!q) return null
          const up = isUp(q.changePercent)
          return (
            <div key={key} className="card">
              <div className="flex items-center justify-between mb-3">
                <span className="text-slate-400 text-sm font-medium">{label}</span>
                <span className={`flex items-center gap-1 text-xs font-semibold ${up ? 'text-success' : 'text-danger'}`}>
                  {up ? <TrendingUp size={14}/> : <TrendingDown size={14}/>}
                  {pct(q.changePercent)}
                </span>
              </div>
              <div className="text-2xl font-bold text-white">₹{fmt(q.price)}</div>
              <div className={`text-sm mt-1 ${up ? 'text-success' : 'text-danger'}`}>
                {up ? '+' : ''}{fmt(q.change)}
              </div>
            </div>
          )
        })}
      </div>

      {/* Quick actions */}
      <div className="grid grid-cols-2 gap-4">
        <button onClick={() => navigate('/chat')}
          className="card text-left hover:border-brand-600/50 transition-all duration-200 group">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 bg-brand-600/20 rounded-xl flex items-center justify-center group-hover:bg-brand-600/30 transition-colors">
              <Activity size={20} className="text-brand-400" />
            </div>
            <span className="font-semibold text-white">AI Chat</span>
            <ArrowUpRight size={16} className="ml-auto text-slate-600 group-hover:text-brand-400 transition-colors" />
          </div>
          <p className="text-sm text-slate-400">Ask anything about markets, stocks, and investments. RAG-grounded answers.</p>
        </button>
        <button onClick={() => navigate('/research')}
          className="card text-left hover:border-brand-600/50 transition-all duration-200 group">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 bg-purple-600/20 rounded-xl flex items-center justify-center group-hover:bg-purple-600/30 transition-colors">
              <TrendingUp size={20} className="text-purple-400" />
            </div>
            <span className="font-semibold text-white">AI Research</span>
            <ArrowUpRight size={16} className="ml-auto text-slate-600 group-hover:text-purple-400 transition-colors" />
          </div>
          <p className="text-sm text-slate-400">Multi-agent deep analysis: Market Data → Sentiment → Fundamentals → Report.</p>
        </button>
      </div>

      {/* Trending stocks */}
      <div className="card">
        <h2 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
          <TrendingUp size={18} className="text-brand-400" /> Top Stocks
        </h2>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-slate-500 text-left border-b border-dark-border">
                <th className="pb-3 font-medium">Symbol</th>
                <th className="pb-3 font-medium">Company</th>
                <th className="pb-3 font-medium text-right">Price</th>
                <th className="pb-3 font-medium text-right">Change</th>
                <th className="pb-3 font-medium text-right">Volume</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-dark-border">
              {trending.map(q => {
                const up = isUp(q.changePercent)
                return (
                  <tr key={q.symbol} className="hover:bg-white/3 transition-colors">
                    <td className="py-3 font-mono text-brand-400 font-semibold">{q.symbol}</td>
                    <td className="py-3 text-slate-300 max-w-[180px] truncate">{q.companyName}</td>
                    <td className="py-3 text-right font-semibold text-white">₹{fmt(q.price)}</td>
                    <td className={`py-3 text-right font-semibold ${up ? 'text-success' : 'text-danger'}`}>
                      {pct(q.changePercent)}
                    </td>
                    <td className="py-3 text-right text-slate-400">{(q.volume/100000).toFixed(1)}L</td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
