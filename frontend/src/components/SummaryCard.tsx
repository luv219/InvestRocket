import { StatCard } from './ui/StatCard'

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
  return <StatCard label={label} value={value} valueClassName={valueClassName} />
}
