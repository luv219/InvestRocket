import { Link, NavLink } from 'react-router-dom'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-lg px-3 py-2 text-sm font-medium ${
    isActive
      ? 'bg-rocket-500/15 text-rocket-400'
      : 'text-slate-300 hover:bg-slate-800 hover:text-white'
  }`

export function Navbar() {
  return (
    <header className="border-b border-slate-800 bg-slate-950/90 backdrop-blur">
      <nav className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <Link to="/" className="flex items-center gap-3 font-semibold text-white">
          <span className="grid size-9 place-items-center rounded-xl bg-rocket-500 text-lg text-slate-950">
            ↗
          </span>
          Invest Rocket
        </Link>
        <div className="flex items-center gap-1">
          <NavLink to="/dashboard" className={navLinkClass}>
            Dashboard
          </NavLink>
          <NavLink to="/login" className={navLinkClass}>
            Login
          </NavLink>
          <Link
            to="/register"
            className="ml-2 rounded-lg bg-rocket-500 px-4 py-2 text-sm font-semibold text-slate-950 hover:bg-rocket-400"
          >
            Register
          </Link>
        </div>
      </nav>
    </header>
  )
}
