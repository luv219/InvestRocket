type MarketStatCardProps = {
  label: string
  value: string
}

export function MarketStatCard({ label, value }: MarketStatCardProps) {
  return (
    <article className="rounded-2xl border border-slate-800 bg-slate-900/60 p-5">
      <p className="text-sm text-slate-500">{label}</p>
      <p className="mt-2 text-lg font-semibold text-white">{value}</p>
    </article>
  )
}
