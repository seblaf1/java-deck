import { useState } from 'react'

export default function App() {
  const [msg, setMsg] = useState<string>('—')

  const ping = async () => {
    try {
      const text = await fetch('/api/health').then(r => r.text())
      setMsg(text)
    } catch (e: any) {
      setMsg('error')
      console.error(e)
    }
  }

  return (
    <div style={{ padding: 24, fontFamily: 'system-ui, sans-serif' }}>
      <h1>Cards Game (Frontend)</h1>
      <button onClick={ping}>Ping Backend</button>
      <div style={{ marginTop: 12 }}>Health: {msg}</div>

      {/* placeholders for later */}
      <hr style={{ margin: '24px 0' }} />
      <div style={{ display: 'grid', gap: 12 }}>
        <button disabled>Create/Delete Game</button>
        <button disabled>Create Deck / Add to Game</button>
        <button disabled>Add/Remove Players</button>
        <button disabled>Deal / Shuffle</button>
        <button disabled>Show Player Hands</button>
        <button disabled>Counts (suits & per-card)</button>
        <button disabled>Rank Players by Value</button>
      </div>
    </div>
  )
}
