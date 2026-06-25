import { Link } from 'react-router-dom'

import { Card } from '../components/ui/Card'

const features = [
  ['Virtual Trading', 'Practice market, limit, and stop-loss orders using simulated funds.'],
  ['Live Demo Prices', 'Follow mock live updates through a backend WebSocket stream.'],
  ['Portfolio Analytics', 'Review allocation, returns, profit and loss, and performance history.'],
  ['Watchlist', 'Track selected companies and move quickly into detailed quote views.'],
  ['Price Alerts', 'Create ABOVE and BELOW thresholds with in-app notifications.'],
  ['Trading Journal', 'Record strategies, decisions, moods, and lessons from each trade.'],
  ['Risk Controls', 'Set order limits and protect the simulator account from oversized actions.'],
  ['Admin Monitoring', 'Inspect users, trading activity, audit logs, and system health.'],
]

const stack = [
  'Java 21 + Spring Boot',
  'React + TypeScript',
  'Neon PostgreSQL',
  'JWT + BCrypt',
  'WebSockets + STOMP',
  'Recharts',
  'Docker',
  'GitHub Actions',
]

export function LandingPage() {
  return (
    <div className="relative isolate overflow-hidden">
      <div className="absolute inset-x-0 top-0 -z-10 h-[34rem] bg-[radial-gradient(circle_at_top,_rgba(16,173,109,0.22),_transparent_65%)]" />
      <section className="mx-auto grid max-w-7xl gap-14 px-5 py-16 sm:px-6 lg:grid-cols-[1.1fr_0.9fr] lg:items-center lg:py-28">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.22em] text-rocket-400">
            Full-stack financial simulator
          </p>
          <h1 className="mt-5 max-w-4xl text-4xl font-bold tracking-tight text-white sm:text-6xl">
            Practice trading. Track performance.
            <span className="block text-rocket-400">Learn markets safely.</span>
          </h1>
          <p className="mt-6 max-w-2xl text-lg leading-8 text-slate-300">
            Search stocks, place virtual orders, monitor a portfolio, test risk
            controls, create alerts, and review every decision without putting
            real money at risk.
          </p>
          <div className="mt-9 flex flex-col gap-3 sm:flex-row">
            <Link
              to="/register"
              className="inline-flex min-h-12 items-center justify-center rounded-xl bg-rocket-500 px-6 py-3 font-semibold text-slate-950 hover:-translate-y-0.5 hover:bg-rocket-400"
            >
              Get Started
            </Link>
            <Link
              to="/login"
              className="inline-flex min-h-12 items-center justify-center rounded-xl border border-slate-700 px-6 py-3 font-semibold text-white hover:border-slate-500 hover:bg-slate-900"
            >
              Login
            </Link>
          </div>
          <p className="mt-6 max-w-2xl text-sm leading-6 text-slate-500">
            Invest Rocket is a virtual trading simulator for educational purposes
            only. It does not provide financial advice and does not execute real
            trades.
          </p>
        </div>

        <Card className="overflow-hidden p-6 sm:p-8">
          <div className="flex items-start justify-between gap-4 border-b border-slate-800 pb-6">
            <div>
              <p className="text-sm text-slate-400">Demo portfolio</p>
              <p className="mt-2 text-3xl font-bold text-white">$100,000.00</p>
              <p className="mt-2 text-sm text-rocket-300">Virtual USD balance</p>
            </div>
            <span className="rounded-full bg-rocket-500/15 px-3 py-1 text-sm font-medium text-rocket-300">
              Simulator
            </span>
          </div>
          <div className="mt-6 grid gap-3 sm:grid-cols-2">
            {['Secure JWT access', 'Mock market provider', 'Advanced orders', 'Analytics dashboard'].map(
              (item) => (
                <div
                  key={item}
                  className="flex items-center gap-3 rounded-xl bg-slate-950/70 p-4 text-sm text-slate-300"
                >
                  <span aria-hidden="true" className="text-rocket-400">✓</span>
                  {item}
                </div>
              ),
            )}
          </div>
        </Card>
      </section>

      <section className="mx-auto max-w-7xl px-5 py-16 sm:px-6">
        <div className="max-w-2xl">
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">
            Product highlights
          </p>
          <h2 className="mt-3 text-3xl font-bold text-white">
            One simulator, the complete workflow
          </h2>
        </div>
        <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {features.map(([title, description]) => (
            <Card key={title} className="p-5">
              <h3 className="font-semibold text-white">{title}</h3>
              <p className="mt-2 text-sm leading-6 text-slate-400">{description}</p>
            </Card>
          ))}
        </div>
      </section>

      <section className="border-y border-slate-800 bg-slate-900/30">
        <div className="mx-auto max-w-7xl px-5 py-16 sm:px-6">
          <h2 className="text-3xl font-bold text-white">Built as a portfolio project</h2>
          <p className="mt-3 max-w-3xl leading-7 text-slate-400">
            Invest Rocket demonstrates transactional backend design, secure API
            boundaries, responsive frontend development, automated testing, and
            deployment preparation.
          </p>
          <div className="mt-7 flex flex-wrap gap-2">
            {stack.map((item) => (
              <span key={item} className="rounded-full border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-slate-300">
                {item}
              </span>
            ))}
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-5 py-16 sm:px-6">
        <Card className="grid gap-6 p-7 sm:p-10 lg:grid-cols-[1fr_auto] lg:items-center">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">
              Demo showcase
            </p>
            <h2 className="mt-3 text-3xl font-bold text-white">
              Screenshots and live demo coming here
            </h2>
            <p className="mt-3 max-w-2xl text-slate-400">
              Capture the dashboard, stock detail, analytics, watchlist, alerts,
              journal, admin experience, and mobile layout before publishing.
            </p>
          </div>
          <Link to="/register" className="inline-flex min-h-12 items-center justify-center rounded-xl bg-rocket-500 px-6 py-3 font-semibold text-slate-950 hover:bg-rocket-400">
            Explore the Simulator
          </Link>
        </Card>
      </section>
    </div>
  )
}
