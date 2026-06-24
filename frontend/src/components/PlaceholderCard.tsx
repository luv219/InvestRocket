import type { ReactNode } from 'react'

type PlaceholderCardProps = {
  eyebrow: string
  title: string
  description: string
  children?: ReactNode
}

export function PlaceholderCard({
  eyebrow,
  title,
  description,
  children,
}: PlaceholderCardProps) {
  return (
    <section className="mx-auto max-w-xl rounded-2xl border border-slate-800 bg-slate-900/70 p-8 shadow-2xl shadow-black/20">
      <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">
        {eyebrow}
      </p>
      <h1 className="mt-3 text-3xl font-bold text-white">{title}</h1>
      <p className="mt-4 leading-7 text-slate-400">{description}</p>
      {children}
    </section>
  )
}
