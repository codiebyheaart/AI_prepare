import { useState, useRef, useEffect } from 'react'
import { aiApi } from '../api/client'
import { Send, Bot, User, Trash2, Sparkles } from 'lucide-react'

interface Message { role: 'user' | 'assistant'; content: string }

const STARTER_QUESTIONS = [
  'What is the KYC requirement for NRI accounts in India?',
  'Explain the differences between NIFTY 50 and SENSEX',
  'What are the risks of investing in small-cap stocks?',
  'How does a mutual fund SIP work?',
]

export default function ChatPage() {
  const [messages, setMessages]   = useState<Message[]>([])
  const [input, setInput]         = useState('')
  const [loading, setLoading]     = useState(false)
  const [sessionId]               = useState<string>(() => crypto.randomUUID())
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading])

  async function send(msg?: string) {
    const text = msg ?? input.trim()
    if (!text || loading) return
    setInput('')
    setMessages(m => [...m, { role: 'user', content: text }])
    setLoading(true)
    try {
      const res = await aiApi.chat(sessionId, text)
      setMessages(m => [...m, { role: 'assistant', content: res.data.data.answer }])
    } catch (e: any) {
      setMessages(m => [...m, { role: 'assistant', content: '⚠️ Sorry, I encountered an error. Please try again.' }])
    } finally { setLoading(false) }
  }

  async function clearChat() {
    await aiApi.clearChat(sessionId)
    setMessages([])
  }

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between px-8 py-5 border-b border-dark-border bg-dark-card/50">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-brand-600/20 rounded-xl flex items-center justify-center">
            <Bot size={20} className="text-brand-400" />
          </div>
          <div>
            <h1 className="font-bold text-white">FinSight AI Chat</h1>
            <p className="text-xs text-slate-500">RAG-grounded · Session: {sessionId.substring(0,8)}...</p>
          </div>
        </div>
        {messages.length > 0 && (
          <button onClick={clearChat} className="btn-ghost flex items-center gap-2 text-xs">
            <Trash2 size={14} /> Clear chat
          </button>
        )}
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-8 py-6 space-y-6">
        {messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full animate-fade-in">
            <div className="w-16 h-16 bg-brand-600/10 rounded-2xl flex items-center justify-center mb-4">
              <Sparkles size={28} className="text-brand-400" />
            </div>
            <h2 className="text-xl font-bold text-white mb-2">Ask FinSight AI</h2>
            <p className="text-slate-400 text-sm mb-8 text-center max-w-sm">
              Ask about markets, stocks, financial regulations, investment strategies — grounded in real documents.
            </p>
            <div className="grid grid-cols-2 gap-3 w-full max-w-lg">
              {STARTER_QUESTIONS.map(q => (
                <button key={q} onClick={() => send(q)}
                  className="card-sm text-left text-sm text-slate-300 hover:text-white hover:border-brand-600/40 transition-all duration-200 hover:bg-brand-600/5">
                  {q}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <>
            {messages.map((m, i) => (
              <div key={i} className={`flex gap-3 animate-slide-up ${m.role === 'user' ? 'flex-row-reverse' : ''}`}>
                <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${
                  m.role === 'user' ? 'bg-brand-600' : 'bg-dark-card border border-dark-border'
                }`}>
                  {m.role === 'user' ? <User size={14} className="text-white" /> : <Bot size={14} className="text-brand-400" />}
                </div>
                <div className={`max-w-[75%] rounded-2xl px-4 py-3 text-sm leading-relaxed ${
                  m.role === 'user'
                    ? 'bg-brand-600 text-white rounded-tr-md'
                    : 'bg-dark-card border border-dark-border text-slate-200 rounded-tl-md'
                }`}>
                  <pre className="whitespace-pre-wrap font-sans">{m.content}</pre>
                </div>
              </div>
            ))}
            {loading && (
              <div className="flex gap-3 animate-slide-up">
                <div className="w-8 h-8 rounded-full bg-dark-card border border-dark-border flex items-center justify-center shrink-0">
                  <Bot size={14} className="text-brand-400" />
                </div>
                <div className="bg-dark-card border border-dark-border rounded-2xl rounded-tl-md px-4 py-4 flex items-center gap-2">
                  <span className="typing-dot" />
                  <span className="typing-dot" />
                  <span className="typing-dot" />
                  <span className="text-xs text-slate-500 ml-1">FinSight is thinking...</span>
                </div>
              </div>
            )}
          </>
        )}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <div className="px-8 py-4 border-t border-dark-border bg-dark-card/30">
        <form onSubmit={e => { e.preventDefault(); send() }} className="flex gap-3">
          <input
            className="input flex-1"
            placeholder="Ask about markets, stocks, financial regulations..."
            value={input}
            onChange={e => setInput(e.target.value)}
            disabled={loading}
          />
          <button type="submit" disabled={loading || !input.trim()} className="btn-primary px-4">
            <Send size={18} />
          </button>
        </form>
        <p className="text-xs text-slate-600 mt-2 text-center">
          Powered by RAG · Grounded in financial documents · Not financial advice
        </p>
      </div>
    </div>
  )
}
