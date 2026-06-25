import type { HTMLAttributes } from 'react'

export function Card({
  className = '',
  ...props
}: HTMLAttributes<HTMLElement>) {
  return (
    <section
      className={`rounded-2xl border border-slate-800 bg-slate-900/60 shadow-sm shadow-black/10 ${className}`}
      {...props}
    />
  )
}
