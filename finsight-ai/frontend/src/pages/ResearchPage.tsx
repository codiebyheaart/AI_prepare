import { useState } from 'react'
import { aiApi } from '../api/client'
import { Search, Bot, CheckCircle, Clock, ChevronDown, ChevronUp } from 'lucide-react'

interface AgentStep { agentName: string; task: string; output: string }
interface Report { id: number; symbol: string; report: string; steps: AgentStep[]; createdAt: string }

const NSE_SYMBOLS = ['RELIANCE','TCS','INFY','HDFCBANK','ICICIBANK','SBIN','BAJFINANCE','WIPRO','ASIANPAINT','MARUTI','TATAMOTORS','SUNPHARMA','TITAN','KOTAKBANK']

export default function ResearchPage() {
  const [symbol, setSymbol]     = useState('HDFCBANK')
  const [loading, setLoading]   = useState(false)
  const [report, setReport]     = useState<Report | null>(null)
  const [expandedStep, setExpandedStep] = useState<number | null>(null)
  const [activeSteps, setActiveSteps]   = useState<number[]>([])

  async function runResearch() {
    setLoading(true); setReport(null); setActiveSteps([]); setExpandedStep(null)

    // Simulate progressive agent steps for UX
    const stepDelay = [1000, 3000, 5500, 8000]
    stepDelay.forEach((delay, i) => setTimeout(() => setActiveSteps(s => [...s, i]), delay))

    try {
      const res = await aiApi.research(symbol)
      setReport(res.data.data)
    } catch (e: any) {
      alert(e.response?.data?.message || 'Research failed. Check your OpenAI API key.')
    } finally { setLoading(false); setActiveSteps([0, 1, 2, 3]) }
  }

  const agentColors = [
    'border-blue-500/30 bg-blue-500/5',
    'border-green-500/30 bg-green-500/5',
    'border-purple-500/30 bg-purple-500/5',
    'border-orange-500/30 bg-orange-500/5',
  ]
  const agentIcons = ['📊','📰','📋','⚖️']

  return (
    <div className="p-8 space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold text-white">AI Research Agent</h1>
        <p className="text-slate-400 mt-1 text-sm">Multi-agent pipeline: Market Data → Sentiment → Fundamentals → Risk → Report</p>
      </div>

      {/* Search bar */}
      <div className="card">
        <div className="flex gap-3">
          <div className="relative flex-1">
            <Search size={16} className="absolute left-3.5 top-3.5 text-slate-500" />
            <select className="input pl-10 appearance-none cursor-pointer"
              value={symbol} onChange={e => setSymbol(e.target.value)}>
              {NSE_SYMBOLS.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <button onClick={runResearch} disabled={loading} className="btn-primary flex items-center gap-2 px-6">
            {loading ? (
              <><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Researching...</>
            ) : (
              <><Bot size={16} /> Run Analysis</>
            )}
          </button>
        </div>
        {loading && (
          <p className="text-xs text-slate-500 mt-3 animate-pulse">
            🤖 Multi-agent pipeline running... This takes 15-30 seconds
          </p>
        )}
      </div>

      {/* Agent Progress */}
      {(loading || report) && (
        <div className="grid grid-cols-4 gap-3">
          {['Market Data Agent','News Sentiment Agent','Fundamental Analysis Agent','Risk Assessment Agent'].map((name, i) => {
            const done = activeSteps.includes(i)
            const realStep = report?.steps?.[i]
            return (
              <div key={i} className={`card border transition-all duration-500 ${done ? agentColors[i] : 'opacity-50'}`}>
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-lg">{agentIcons[i]}</span>
                  {done ? <CheckCircle size={14} className="text-success ml-auto" /> : <Clock size={14} className="text-slate-500 ml-auto animate-pulse" />}
                </div>
                <div className="text-sm font-semibold text-white">{name}</div>
                <div className="text-xs text-slate-500 mt-1">{realStep?.task ?? 'Waiting...'}</div>
                {done && realStep && (
                  <button onClick={() => setExpandedStep(expandedStep === i ? null : i)}
                    className="flex items-center gap-1 text-xs text-brand-400 mt-2 hover:text-brand-300 transition-colors">
                    {expandedStep === i ? <><ChevronUp size={12}/>Hide</> : <><ChevronDown size={12}/>View output</>}
                  </button>
                )}
                {expandedStep === i && realStep && (
                  <div className="mt-3 text-xs text-slate-400 bg-dark-900/50 rounded-lg p-3 max-h-40 overflow-y-auto whitespace-pre-wrap">
                    {realStep.output}
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}

      {/* Final Report */}
      {report?.report && (
        <div className="card border-brand-600/20 animate-slide-up">
          <div className="flex items-center gap-3 mb-5 pb-4 border-b border-dark-border">
            <div className="w-10 h-10 bg-brand-600/20 rounded-xl flex items-center justify-center">
              <Bot size={20} className="text-brand-400" />
            </div>
            <div>
              <h2 className="font-bold text-white">Investment Research Report</h2>
              <p className="text-xs text-slate-500">{symbol} · Generated by FinSight AI · {new Date(report.createdAt).toLocaleString()}</p>
            </div>
          </div>
          <div className="prose prose-invert prose-sm max-w-none">
            <pre className="whitespace-pre-wrap text-slate-300 text-sm leading-relaxed font-sans">
              {report.report}
            </pre>
          </div>
        </div>
      )}
    </div>
  )
}
