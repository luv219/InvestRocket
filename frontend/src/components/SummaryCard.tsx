type SummaryCardProps = {
  label: string
  value: string
  valueClassName?: string
}

export function SummaryCard({
  label,
  value,
  valueClassName = 'text-white',
}: SummaryCardProps) {
  return (
    <article className="rounded-2xl border border-slate-800 bg-slate-900/60 p-5">
      <p className="text-sm text-slate-500">{label}</p>
      <p className={`mt-3 text-xl font-bold ${valueClassName}`}>{value}</p>
    </article>
  )
}
