import { useAuth } from '../features/auth/useAuth'

const placeholders = [
  { label: 'Virtual Balance', value: 'Coming in next phase' },
  { label: 'Portfolio Value', value: 'Coming in next phase' },
  { label: 'Orders', value: 'Coming in next phase' },
  { label: 'Market Data', value: 'Coming in next phase' },
]

export function DashboardPage() {
  const { user, logout } = useAuth()

  return (
    <div className="mx-auto max-w-6xl px-6 py-14">
      <section className="flex flex-col gap-6 rounded-3xl border border-slate-800 bg-slate-900/70 p-8 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">
            Trading simulator
          </p>
          <h1 className="mt-3 text-3xl font-bold text-white">
            Welcome, {user?.fullName}
          </h1>
          <p className="mt-2 text-slate-400">{user?.email}</p>
        </div>
        <button
          type="button"
          onClick={logout}
          className="rounded-xl border border-slate-700 px-5 py-3 font-semibold text-white hover:border-slate-500 hover:bg-slate-800"
        >
          Logout
        </button>
      </section>

      <section className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {placeholders.map((item) => (
          <article
            key={item.label}
            className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6"
          >
            <p className="text-sm font-medium text-slate-400">{item.label}</p>
            <p className="mt-4 text-lg font-semibold text-white">{item.value}</p>
          </article>
        ))}
      </section>

      <p className="mt-8 rounded-xl border border-amber-400/20 bg-amber-400/5 px-5 py-4 text-sm text-amber-200">
        Invest Rocket uses virtual funds only and does not provide financial
        advice.
      </p>
    </div>
  )
}
