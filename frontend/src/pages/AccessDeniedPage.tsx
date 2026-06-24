import { Link } from 'react-router-dom'

export function AccessDeniedPage() {
  return (
    <div className="mx-auto max-w-xl px-6 py-20 text-center">
      <p className="text-sm font-semibold uppercase tracking-[0.2em] text-red-400">
        Access denied
      </p>
      <h1 className="mt-4 text-3xl font-bold text-white">
        You do not have permission to access this page.
      </h1>
      <Link
        to="/dashboard"
        className="mt-7 inline-flex rounded-xl bg-rocket-500 px-5 py-3 font-semibold text-slate-950"
      >
        Return to Dashboard
      </Link>
    </div>
  )
}
