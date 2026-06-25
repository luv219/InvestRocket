export function StatCard({
  label,
  value,
  valueClassName = 'text-white',
  helper,
}: {
  label: string
  value: string
  valueClassName?: string
  helper?: string
}) {
  return (
    <article className="rounded-2xl border border-slate-800 bg-slate-900/60 p-5 shadow-sm shadow-black/10">
      <p className="text-sm font-medium text-slate-400">{label}</p>
      <p className={`mt-3 text-xl font-bold tracking-tight ${valueClassName}`}>
        {value}
      </p>
      {helper && <p className="mt-2 text-xs text-slate-500">{helper}</p>}
    </article>
  )
}
