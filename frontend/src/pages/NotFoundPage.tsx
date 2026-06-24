import { Link } from 'react-router-dom'

import { PlaceholderCard } from '../components/PlaceholderCard'

export function NotFoundPage() {
  return (
    <div className="px-6 py-20">
      <PlaceholderCard
        eyebrow="404"
        title="Page not found"
        description="The page you requested does not exist in the Invest Rocket application."
      >
        <Link
          to="/"
          className="mt-7 inline-flex rounded-lg bg-rocket-500 px-4 py-2 text-sm font-semibold text-slate-950 hover:bg-rocket-400"
        >
          Return home
        </Link>
      </PlaceholderCard>
    </div>
  )
}
