import type { ReactNode } from 'react'

type AlertTone = 'error' | 'success' | 'warning' | 'info'

const tones: Record<AlertTone, string> = {
  error: 'border-red-500/30 bg-red-500/10 text-red-200',
  success: 'border-rocket-500/30 bg-rocket-500/10 text-rocket-200',
  warning: 'border-amber-400/30 bg-amber-400/10 text-amber-100',
  info: 'border-blue-500/30 bg-blue-500/10 text-blue-100',
}

export function Alert({
  children,
  tone = 'info',
}: {
  children: ReactNode
  tone?: AlertTone
}) {
  return (
    <div
      role={tone === 'error' ? 'alert' : 'status'}
      className={`rounded-xl border px-4 py-3 text-sm ${tones[tone]}`}
    >
      {children}
    </div>
  )
}
