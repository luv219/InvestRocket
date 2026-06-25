import type { HTMLAttributes, ReactNode } from 'react'

export function TableContainer({
  children,
  className = '',
}: {
  children: ReactNode
  className?: string
}) {
  return (
    <div
      className={`overflow-x-auto rounded-2xl border border-slate-800 ${className}`}
      tabIndex={0}
      aria-label="Scrollable data table"
    >
      {children}
    </div>
  )
}

export function Table({
  className = '',
  ...props
}: HTMLAttributes<HTMLTableElement>) {
  return (
    <table
      className={`min-w-full divide-y divide-slate-800 text-left text-sm ${className}`}
      {...props}
    />
  )
}
