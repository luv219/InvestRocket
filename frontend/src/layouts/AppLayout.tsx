import { Outlet } from 'react-router-dom'

import { Navbar } from '../components/Navbar'

export function AppLayout() {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <Navbar />
      <main>
        <Outlet />
      </main>
      <footer className="border-t border-slate-800 px-6 py-6 text-center text-sm text-slate-500">
        Invest Rocket is a virtual trading simulator for educational purposes
        only. It does not provide financial advice and does not execute real
        trades.
      </footer>
    </div>
  )
}
