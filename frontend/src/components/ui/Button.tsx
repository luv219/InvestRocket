import type { ButtonHTMLAttributes } from 'react'

type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost'

const variants: Record<ButtonVariant, string> = {
  primary: 'bg-rocket-500 text-slate-950 hover:bg-rocket-400',
  secondary:
    'border border-slate-700 bg-slate-900 text-white hover:border-slate-500 hover:bg-slate-800',
  danger:
    'border border-red-500/40 bg-red-500/10 text-red-200 hover:bg-red-500/20',
  ghost: 'text-slate-300 hover:bg-slate-800 hover:text-white',
}

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant
}

export function Button({
  variant = 'primary',
  className = '',
  type = 'button',
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      className={`inline-flex min-h-11 items-center justify-center rounded-xl px-5 py-2.5 text-sm font-semibold disabled:cursor-not-allowed disabled:opacity-50 ${variants[variant]} ${className}`}
      {...props}
    />
  )
}
