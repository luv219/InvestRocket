import { Link } from 'react-router-dom'

const plannedFeatures = [
  'Virtual cash and portfolio positions',
  'Near real-time stock market data',
  'Simulated buy and sell orders',
]

export function LandingPage() {
  return (
    <div className="relative isolate overflow-hidden">
      <div className="absolute inset-x-0 top-0 -z-10 h-96 bg-[radial-gradient(circle_at_top,_rgba(16,173,109,0.2),_transparent_60%)]" />
      <section className="mx-auto grid max-w-6xl gap-14 px-6 py-20 lg:grid-cols-[1.15fr_0.85fr] lg:items-center lg:py-28">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.22em] text-rocket-400">
            Learn markets without risking money
          </p>
          <h1 className="mt-5 max-w-3xl text-5xl font-bold tracking-tight text-white sm:text-6xl">
            Practice investing.
            <span className="block text-rocket-400">Build confidence.</span>
          </h1>
          <p className="mt-6 max-w-2xl text-lg leading-8 text-slate-300">
            Invest Rocket is an educational stock trading simulator for exploring
            markets, testing ideas, and tracking a virtual portfolio.
          </p>
          <div className="mt-9 flex flex-wrap gap-4">
            <Link
              to="/register"
              className="rounded-xl bg-rocket-500 px-6 py-3 font-semibold text-slate-950 hover:-translate-y-0.5 hover:bg-rocket-400"
            >
              Create an account
            </Link>
            <Link
              to="/login"
              className="rounded-xl border border-slate-700 px-6 py-3 font-semibold text-white hover:border-slate-500 hover:bg-slate-900"
            >
              Login
            </Link>
          </div>
          <p className="mt-6 max-w-2xl text-sm text-slate-500">
            Invest Rocket is a virtual trading simulator for educational purposes
            only. It does not provide financial advice and does not execute real
            trades.
          </p>
        </div>

        <div className="rounded-3xl border border-slate-800 bg-slate-900/80 p-6 shadow-2xl shadow-rocket-950/40">
          <div className="flex items-center justify-between border-b border-slate-800 pb-5">
            <div>
              <p className="text-sm text-slate-500">Virtual portfolio</p>
              <p className="mt-1 text-3xl font-bold text-white">$100,000.00</p>
            </div>
            <span className="rounded-full bg-rocket-500/15 px-3 py-1 text-sm font-medium text-rocket-400">
              Demo
            </span>
          </div>
          <div className="mt-6 space-y-4">
            {plannedFeatures.map((feature) => (
              <div
                key={feature}
                className="flex items-center gap-3 rounded-xl bg-slate-950/70 p-4 text-slate-300"
              >
                <span className="grid size-7 place-items-center rounded-full bg-rocket-500/15 text-rocket-400">
                  ✓
                </span>
                {feature}
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  )
}
