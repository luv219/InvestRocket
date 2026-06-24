import { Link } from 'react-router-dom'

import { PlaceholderCard } from '../components/PlaceholderCard'

export function RegisterPage() {
  return (
    <div className="px-6 py-20">
      <PlaceholderCard
        eyebrow="Phase 1"
        title="Create your account"
        description="Registration and virtual starting funds will be added with the user and wallet modules."
      >
        <Link
          to="/"
          className="mt-7 inline-flex rounded-lg bg-slate-800 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-700"
        >
          Back to home
        </Link>
      </PlaceholderCard>
    </div>
  )
}
