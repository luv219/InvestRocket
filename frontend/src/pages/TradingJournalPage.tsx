import { useEffect, useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router-dom'

import {
  createEntry,
  deleteEntry,
  getEntries,
  updateEntry,
} from '../features/journal/journalService'
import type {
  CreateJournalEntryRequest,
  JournalEntry,
  JournalMood,
} from '../types/journal'
import { getApiErrorMessage } from '../utils/apiError'
import { formatDateTime } from '../utils/formatters'

const moods: JournalMood[] = [
  'CONFIDENT',
  'NEUTRAL',
  'UNCERTAIN',
  'FRUSTRATED',
  'EXCITED',
]

export function TradingJournalPage() {
  const [params] = useSearchParams()
  const [entries, setEntries] = useState<JournalEntry[]>([])
  const [editingId, setEditingId] = useState<string | null>(null)
  const [filter, setFilter] = useState('')
  const [error, setError] = useState('')
  const [form, setForm] = useState<CreateJournalEntryRequest>({
    title: '',
    content: '',
    mood: 'NEUTRAL',
    strategy: '',
    symbol: params.get('symbol') ?? '',
    orderId: params.get('orderId') ?? undefined,
    tradeId: params.get('tradeId') ?? undefined,
    tags: '',
  })

  useEffect(() => {
    getEntries().then(setEntries).catch((requestError) =>
      setError(getApiErrorMessage(requestError, 'Unable to load journal')),
    )
  }, [])

  function resetForm() {
    setEditingId(null)
    setForm({
      title: '',
      content: '',
      mood: 'NEUTRAL',
      strategy: '',
      symbol: '',
      tags: '',
    })
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError('')
    try {
      if (editingId) {
        const updated = await updateEntry(editingId, {
          title: form.title,
          content: form.content,
          mood: form.mood,
          strategy: form.strategy,
          symbol: form.symbol,
          tags: form.tags,
        })
        setEntries((current) =>
          current.map((entry) => (entry.id === editingId ? updated : entry)),
        )
      } else {
        const created = await createEntry(form)
        setEntries((current) => [created, ...current])
      }
      resetForm()
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to save journal entry'))
    }
  }

  function edit(entry: JournalEntry) {
    setEditingId(entry.id)
    setForm({
      title: entry.title,
      content: entry.content,
      mood: entry.mood ?? 'NEUTRAL',
      strategy: entry.strategy ?? '',
      symbol: entry.symbol ?? '',
      tags: entry.tags ?? '',
    })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function remove(id: string) {
    await deleteEntry(id)
    setEntries((current) => current.filter((entry) => entry.id !== id))
  }

  const visibleEntries = entries.filter((entry) =>
    filter.trim()
      ? entry.symbol?.toUpperCase() === filter.trim().toUpperCase()
      : true,
  )

  return (
    <div className="mx-auto max-w-6xl px-6 py-14">
      <h1 className="text-4xl font-bold text-white">Trading Journal</h1>
      <p className="mt-3 text-slate-400">Record decisions, strategies, and lessons from simulated trades.</p>
      <form onSubmit={submit} className="mt-8 grid gap-4 rounded-2xl border border-slate-800 bg-slate-900/60 p-6 md:grid-cols-2">
        <input required maxLength={150} value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} placeholder="Entry title" className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white" />
        <select value={form.mood} onChange={(event) => setForm({ ...form, mood: event.target.value as JournalMood })} className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white">
          {moods.map((mood) => <option key={mood}>{mood}</option>)}
        </select>
        <textarea required maxLength={5000} value={form.content} onChange={(event) => setForm({ ...form, content: event.target.value })} placeholder="What happened and what did you learn?" rows={5} className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white md:col-span-2" />
        <input value={form.strategy} onChange={(event) => setForm({ ...form, strategy: event.target.value })} placeholder="Strategy" className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white" />
        <input value={form.symbol} onChange={(event) => setForm({ ...form, symbol: event.target.value.toUpperCase() })} placeholder="Symbol (optional)" className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 uppercase text-white" />
        <input value={form.tags} onChange={(event) => setForm({ ...form, tags: event.target.value })} placeholder="Tags: breakout,demo" className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white md:col-span-2" />
        <div className="flex gap-3 md:col-span-2">
          <button className="rounded-xl bg-rocket-500 px-6 py-3 font-semibold text-slate-950">{editingId ? 'Update Entry' : 'Create Entry'}</button>
          {editingId && <button type="button" onClick={resetForm} className="rounded-xl border border-slate-700 px-6 py-3 font-semibold text-white">Cancel edit</button>}
        </div>
      </form>
      {error && <p className="mt-5 text-red-300">{error}</p>}
      <div className="mt-10 flex flex-wrap items-center justify-between gap-4">
        <h2 className="text-2xl font-bold text-white">Journal entries</h2>
        <input value={filter} onChange={(event) => setFilter(event.target.value)} placeholder="Filter by symbol" className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-2 uppercase text-white" />
      </div>
      {visibleEntries.length === 0 ? (
        <p className="mt-6 rounded-2xl border border-dashed border-slate-700 p-12 text-center text-slate-500">No journal entries yet. Record your first trading note.</p>
      ) : (
        <div className="mt-6 grid gap-5 md:grid-cols-2">
          {visibleEntries.map((entry) => (
            <article key={entry.id} className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6">
              <div className="flex justify-between gap-4">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wide text-rocket-400">{entry.symbol ?? 'GENERAL'} {entry.mood ? `• ${entry.mood}` : ''}</p>
                  <h3 className="mt-2 text-xl font-bold text-white">{entry.title}</h3>
                </div>
                <div className="flex gap-3">
                  <button type="button" onClick={() => edit(entry)} className="text-sm font-semibold text-rocket-400">Edit</button>
                  <button type="button" onClick={() => void remove(entry.id)} className="text-sm font-semibold text-red-400">Delete</button>
                </div>
              </div>
              <p className="mt-4 whitespace-pre-wrap text-slate-300">{entry.content}</p>
              {entry.strategy && <p className="mt-4 text-sm text-slate-400">Strategy: {entry.strategy}</p>}
              {entry.tags && <p className="mt-2 text-sm text-slate-500">Tags: {entry.tags}</p>}
              <p className="mt-4 text-xs text-slate-500">{formatDateTime(entry.updatedAt)}</p>
            </article>
          ))}
        </div>
      )}
    </div>
  )
}
