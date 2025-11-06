import { useEffect, useState } from 'react'

type GameDto = { id: string; createdAt: string }
type PlayerDto = { playerId: string; playerName: string; totalValue: number }
type CardDto = { suit: number; rank: number }
type SuitCountDto = { suit: string; remaining: number }
type CardCountDto = { suit: number; rank: number; count: number }

const SUITS = ['♥', '♠', '♣', '♦']
const RANKS = ['A', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K']

export default function App() {
  const [games, setGames] = useState<GameDto[]>([])
  const [selectedGame, setSelectedGame] = useState<GameDto | null>(null)
  const [players, setPlayers] = useState<PlayerDto[]>([])
  const [cards, setCards] = useState<Record<string, CardDto[]>>({})
  const [suits, setSuits] = useState<SuitCountDto[]>([])
  const [suitRanks, setSuitRanks] = useState<CardCountDto[]>([])
  const [loading, setLoading] = useState(false)
  const [newPlayerName, setNewPlayerName] = useState('')
  const [isAddingDeck, setIsAddingDeck] = useState(false)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)

  // --- helpers
  const api = async (path: string, opts?: RequestInit) => {
    const res = await fetch(`/api${path}`, opts)
    if (!res.ok) throw new Error(`${path}: HTTP ${res.status}`)
    return res
  }

  const showError = (msg: string) => {
    setErrorMsg(msg)
    setTimeout(() => setErrorMsg(null), 5000)
  }

  const refreshGames = async () => {
    setLoading(true)
    try {
      const data: GameDto[] = await api('/games').then(r => r.json())
      setGames(data)
    } finally {
      setLoading(false)
    }
  }

  const createGame = async () => {
    try {
      await api('/games', { method: 'POST' })
      await refreshGames()
    } catch (err) {
      console.error(err)
      showError('Failed to create game.')
    }
  }

  const deleteGame = async (id: string) => {
    if (!confirm('Delete this game?')) return
    try {
      await api(`/games/${id}`, { method: 'DELETE' })
      setSelectedGame(null)
      await refreshGames()
    } catch (err) {
      console.error(err)
      showError('Failed to delete game.')
    }
  }

  const shuffleGame = async () => {
    if (!selectedGame) return
    try {
      await api(`/games/${selectedGame.id}/shuffle`, { method: 'POST' })
      await fullRefresh()
    } catch (err) {
      console.error(err)
      showError('Shuffle failed.')
    }
  }

  const addPlayerToGame = async () => {
    if (!selectedGame || !newPlayerName.trim()) return
    const encoded = encodeURIComponent(newPlayerName.trim())
    try {
      await api(`/games/${selectedGame.id}/join?playerName=${encoded}`, { method: 'POST' })
      setNewPlayerName('')
      await fullRefresh()
    } catch (err) {
      console.error(err)
      showError('Failed to add player.')
    }
  }

  const leavePlayer = async (playerId: string) => {
    try {
      await api(`/players/${playerId}/leave`, { method: 'DELETE' })
      await fullRefresh()
    } catch (err) {
      console.error(err)
      showError('Failed to remove player.')
    }
  }

  const createAndAddDeck = async () => {
    if (!selectedGame || isAddingDeck) return
    setIsAddingDeck(true)
    try {
      const deckId: string = await api('/games/new-deck', { method: 'POST' }).then(r => r.json())
      await api(`/games/${selectedGame.id}/decks/${deckId}`, { method: 'POST' })
      await fullRefresh()
    } catch (err) {
      console.error(err)
      showError('Failed to create or add deck.')
    } finally {
      setIsAddingDeck(false)
    }
  }

  const dealCard = async (playerId: string) => {
    if (!selectedGame) return
    try {
      await api(`/games/${selectedGame.id}/players/${playerId}/deal?count=1`, { method: 'POST' })
      await fullRefresh()
    } catch (err) {
      console.error(err)
      showError('Failed to deal card.')
    }
  }

  const loadPlayers = async () => {
    if (!selectedGame) return
    try {
      const data: PlayerDto[] = await api(`/games/${selectedGame.id}/players`).then(r => r.json())
      setPlayers(data)
      for (const p of data) {
        await loadPlayerCards(p.playerId)
      }
    } catch (err) {
      console.error(err)
      showError('Failed to load players.')
    }
  }

  const loadPlayerCards = async (playerId: string) => {
    try {
      const data: CardDto[] = await api(`/players/${playerId}/hand`).then(r => r.json())
      setCards(prev => ({ ...prev, [playerId]: data }))
    } catch (err) {
      console.error(err)
      showError('Failed to load hand.')
    }
  }

  const loadRemaining = async () => {
    if (!selectedGame) return
    try {
      const [bySuit, bySuitRank] = await Promise.all([
        api(`/games/${selectedGame.id}/remaining-by-suit`).then(r => r.json()),
        api(`/games/${selectedGame.id}/remaining-by-suit-rank`).then(r => r.json())
      ])
      setSuits(bySuit)
      setSuitRanks(bySuitRank)
    } catch (err) {
      console.error(err)
      showError('Failed to load remaining cards.')
    }
  }

  const fullRefresh = async () => {
    await Promise.all([loadPlayers(), loadRemaining()])
  }

  // Poll every 5 s for live updates
  useEffect(() => {
    let interval: any
    if (selectedGame) {
      interval = setInterval(() => {
        fullRefresh()
      }, 5000)
      fullRefresh()
    }
    return () => clearInterval(interval)
  }, [selectedGame])

  // Initial game list
  useEffect(() => {
    refreshGames()
  }, [])

  return (
    <div style={{ padding: 24, fontFamily: 'system-ui, sans-serif' }}>
      <h1>Card Game Dashboard</h1>

      {errorMsg && (
        <div
          style={{
            background: '#fdd',
            color: '#900',
            padding: '8px 12px',
            borderRadius: 4,
            marginBottom: 12,
          }}
        >
          {errorMsg}
        </div>
      )}

      <div style={{ marginBottom: 16 }}>
        <button onClick={createGame}>Create Game</button>
        <button onClick={refreshGames} style={{ marginLeft: 8 }}>
          Refresh List
        </button>
        {loading && <span style={{ marginLeft: 8 }}>Loading...</span>}
      </div>

      {!selectedGame ? (
        <>
          <h2>Games</h2>
          {games.length === 0 && <div>No games.</div>}
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {games.map(g => (
              <li key={g.id} style={{ border: '1px solid #ccc', padding: 12, marginBottom: 8 }}>
                <b>ID:</b> {g.id}
                <br />
                <b>Created:</b> {new Date(g.createdAt).toLocaleString()}
                <br />
                <button onClick={() => setSelectedGame(g)}>Open</button>{' '}
                <button style={{ color: 'red' }} onClick={() => deleteGame(g.id)}>
                  Delete
                </button>
              </li>
            ))}
          </ul>
        </>
      ) : (
        <>
          <h2>Game {selectedGame.id}</h2>
          <button onClick={() => setSelectedGame(null)}>← Back</button>{' '}
          <button onClick={shuffleGame}>Shuffle Deck</button>

          <hr />

          <h3>Deck</h3>
          <button onClick={createAndAddDeck} disabled={isAddingDeck}>
            {isAddingDeck ? 'Adding Deck...' : 'Add Deck'}
          </button>

          <hr />

          <h3>Players</h3>
          <div style={{ display: 'flex', gap: 8 }}>
            <input
              placeholder="Player name"
              value={newPlayerName}
              onChange={e => setNewPlayerName(e.target.value)}
            />
            <button onClick={addPlayerToGame}>Add Player</button>
          </div>

          {players.length > 0 && (
            <table border={1} cellPadding={6} style={{ marginTop: 12, borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Total Value</th>
                  <th>Cards</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {players.map(p => {
                  const hand = cards[p.playerId] || []
                  return (
                    <tr key={p.playerId}>
                      <td>{p.playerName}</td>
                      <td>{p.totalValue}</td>
                      <td>
                        {hand.length > 0
                          ? hand
                              .map(
                                c =>
                                  `${SUITS[c.suit]}${
                                    RANKS[c.rank]
                                  }`
                              )
                              .join(' ')
                          : '—'}
                      </td>
                      <td>
                        <button onClick={() => dealCard(p.playerId)}>Deal 1</button>{' '}
                        <button onClick={() => leavePlayer(p.playerId)}>Remove</button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}

          <hr />

          <h3>Remaining Cards in Shoe</h3>

          {/* Suit Summary Table */}
          <table
            border={1}
            cellPadding={6}
            style={{
              borderCollapse: 'collapse',
              marginBottom: 12,
              textAlign: 'center',
              minWidth: '200px',
            }}
          >
            <thead>
              <tr>
                {SUITS.map(s => (
                  <th
                    key={s}
                    style={{
                      color: s === '♥' || s === '♦' ? 'red' : 'black',
                      fontSize: '18px',
                      width: 40,
                    }}
                  >
                    {s}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              <tr>
                {SUITS.map(s => {
                  const item = suits.find(i => i.suit === s)
                  return <td key={s}>{item ? item.remaining : 0}</td>
                })}
              </tr>
            </tbody>
          </table>

          {/* Detailed Rank Table */}
          <table
            border={1}
            cellPadding={6}
            style={{
              borderCollapse: 'collapse',
              textAlign: 'center',
              tableLayout: 'fixed',
              width: '100%',
              fontFamily: 'monospace',
            }}
          >
            <thead>
              <tr>
                {RANKS.map(r => (
                  <th key={r} style={{ width: '2ch' }}>
                    {r}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {SUITS.map((suit, sIdx) => (
                <tr key={suit}>
                  {RANKS.map((_, rIdx) => {
                    const card = suitRanks.find(c => c.suit === sIdx && c.rank === rIdx)
                    const color = suit === '♥' || suit === '♦' ? 'red' : 'black'
                    return (
                      <td key={rIdx} style={{ color }}>
                        {card ? card.count : 0}
                      </td>
                    )
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </div>
  )
}
