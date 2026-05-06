import { useEffect, useState } from 'react'
import { portfolioApi } from '../api/client'
import { Plus, Trash2, TrendingUp, TrendingDown } from 'lucide-react'

interface Holding {
  id: number; symbol: string; companyName: string; quantity: number
  avgBuyPrice: number; currentPrice: number; currentValue: number
  pnl: number; pnlPercent: number
}
interface Portfolio {
  id: number; name: string; holdings: Holding[]
  totalInvested: number; totalCurrentValue: number
  totalPnl: number; totalPnlPercent: number
}

export default function PortfolioPage() {
  const [portfolio, setPortfolio] = useState<Portfolio | null>(null)
  const [loading, setLoading]     = useState(true)
  const [adding, setAdding]       = useState(false)
  const [showForm, setShowForm]   = useState(false)
  const [form, setForm] = useState({ symbol: '', companyName: '', quantity: '', avgBuyPrice: '' })

  async function load() {
    try {
      const res = await portfolioApi.get()
      setPortfolio(res.data.data)
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  async function addHolding(e: React.FormEvent) {
    e.preventDefault(); setAdding(true)
    try {
      await portfolioApi.addHolding({
        symbol: form.symbol.toUpperCase(),
        companyName: form.companyName,
        quantity: parseFloat(form.quantity),
        avgBuyPrice: parseFloat(form.avgBuyPrice),
      })
      setForm({ symbol: '', companyName: '', quantity: '', avgBuyPrice: '' })
      setShowForm(false)
      await load()
    } catch (e: any) {
      alert(e.response?.data?.message || 'Failed to add holding')
    } finally { setAdding(false) }
  }

  async function remove(id: number) {
    if (!confirm('Remove this holding?')) return
    await portfolioApi.removeHolding(id)
    await load()
  }

  function fmt(n: number) { return n?.toLocaleString('en-IN', { maximumFractionDigits: 2 }) ?? '-' }
  function pct(n: number) { return (n > 0 ? '+' : '') + n?.toFixed(2) + '%' }

  if (loading) return <div className="flex items-center justify-center h-full"><div className="w-8 h-8 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" /></div>

  const p = portfolio
  const up = (p?.totalPnl ?? 0) >= 0

  return (
    <div className="p-8 space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white">My Portfolio</h1>
          <p className="text-slate-400 text-sm mt-1">Track your holdings and P&amp;L</p>
        </div>
        <button onClick={() => setShowForm(f => !f)} className="btn-primary flex items-center gap-2">
          <Plus size={16} /> Add Holding
        </button>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-4 gap-4">
        {[
          { label: 'Total Invested', val: `₹${fmt(p?.totalInvested ?? 0)}`, color: 'text-white' },
          { label: 'Current Value', val: `₹${fmt(p?.totalCurrentValue ?? 0)}`, color: 'text-white' },
          { label: 'Total P&L', val: `₹${fmt(Math.abs(p?.totalPnl ?? 0))}`, color: up ? 'text-success' : 'text-danger', prefix: p?.totalPnl ?? 0 >= 0 ? '+' : '-' },
          { label: 'Returns', val: pct(p?.totalPnlPercent ?? 0), color: up ? 'text-success' : 'text-danger' },
        ].map(({ label, val, color, prefix }) => (
          <div key={label} className="card">
            <div className="text-slate-400 text-xs mb-1">{label}</div>
            <div className={`text-xl font-bold ${color}`}>{prefix}{val}</div>
          </div>
        ))}
      </div>

      {/* Add holding form */}
      {showForm && (
        <div className="card border-brand-600/30 animate-slide-up">
          <h3 className="font-semibold text-white mb-4">Add New Holding</h3>
          <form onSubmit={addHolding} className="grid grid-cols-4 gap-3">
            <input className="input" placeholder="Symbol (e.g. TCS)" required
              value={form.symbol} onChange={e => setForm(f => ({ ...f, symbol: e.target.value }))} />
            <input className="input" placeholder="Company name"
              value={form.companyName} onChange={e => setForm(f => ({ ...f, companyName: e.target.value }))} />
            <input className="input" type="number" step="0.0001" placeholder="Quantity" required
              value={form.quantity} onChange={e => setForm(f => ({ ...f, quantity: e.target.value }))} />
            <input className="input" type="number" step="0.01" placeholder="Avg Buy Price (₹)" required
              value={form.avgBuyPrice} onChange={e => setForm(f => ({ ...f, avgBuyPrice: e.target.value }))} />
            <div className="col-span-4 flex gap-3">
              <button type="submit" disabled={adding} className="btn-primary">
                {adding ? 'Adding...' : 'Add Holding'}
              </button>
              <button type="button" onClick={() => setShowForm(false)} className="btn-ghost">Cancel</button>
            </div>
          </form>
        </div>
      )}

      {/* Holdings table */}
      <div className="card">
        <h2 className="font-bold text-white mb-4">Holdings ({p?.holdings?.length ?? 0})</h2>
        {!p?.holdings?.length ? (
          <div className="text-center py-12 text-slate-500">
            <TrendingUp size={40} className="mx-auto mb-3 opacity-30" />
            <p>No holdings yet. Add your first stock!</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-slate-500 text-left border-b border-dark-border">
                  {['Symbol','Company','Qty','Avg Buy','Current','Value','P&L','%',''].map(h => (
                    <th key={h} className={`pb-3 font-medium ${['Value','P&L','%'].includes(h) ? 'text-right' : ''}`}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-dark-border">
                {p.holdings.map(h => {
                  const up = h.pnl >= 0
                  return (
                    <tr key={h.id} className="hover:bg-white/3 transition-colors">
                      <td className="py-3 font-mono text-brand-400 font-semibold">{h.symbol}</td>
                      <td className="py-3 text-slate-300 max-w-[150px] truncate">{h.companyName || h.symbol}</td>
                      <td className="py-3 text-white">{h.quantity}</td>
                      <td className="py-3 text-slate-400">₹{fmt(h.avgBuyPrice)}</td>
                      <td className="py-3 text-white">₹{fmt(h.currentPrice)}</td>
                      <td className="py-3 text-right text-white font-semibold">₹{fmt(h.currentValue)}</td>
                      <td className={`py-3 text-right font-semibold ${up ? 'text-success' : 'text-danger'}`}>
                        {up ? '+' : ''}₹{fmt(Math.abs(h.pnl))}
                      </td>
                      <td className={`py-3 text-right font-semibold ${up ? 'text-success' : 'text-danger'}`}>
                        <span className="flex items-center justify-end gap-1">
                          {up ? <TrendingUp size={12}/> : <TrendingDown size={12}/>}
                          {pct(h.pnlPercent)}
                        </span>
                      </td>
                      <td className="py-3 text-right">
                        <button onClick={() => remove(h.id)} className="text-slate-600 hover:text-danger transition-colors p-1 rounded-lg hover:bg-danger/10">
                          <Trash2 size={14} />
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
