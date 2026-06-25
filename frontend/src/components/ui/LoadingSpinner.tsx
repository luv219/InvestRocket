export function LoadingSpinner({ label = 'Loading...' }: { label?: string }) {
  return (
    <div
      role="status"
      className="flex min-h-48 items-center justify-center gap-3 text-slate-400"
    >
      <span className="size-5 animate-spin rounded-full border-2 border-slate-700 border-t-rocket-400" />
      <span>{label}</span>
    </div>
  )
}
