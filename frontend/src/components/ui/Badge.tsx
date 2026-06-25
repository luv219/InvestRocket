import type { ReactNode } from 'react'

type BadgeTone = 'neutral' | 'success' | 'danger' | 'warning' | 'info'

const tones: Record<BadgeTone, string> = {
  neutral: 'bg-slate-700/70 text-slate-200',
  success: 'bg-rocket-500/15 text-rocket-300',
  danger: 'bg-red-500/15 text-red-300',
  warning: 'bg-amber-400/15 text-amber-200',
  info: 'bg-blue-500/15 text-blue-200',
}

export function Badge({
  children,
  tone = 'neutral',
}: {
  children: ReactNode
  tone?: BadgeTone
}) {
  return (
    <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${tones[tone]}`}>
      {children}
    </span>
  )
}
