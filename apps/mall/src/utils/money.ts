/** Format minor units (cents) as yuan string with 2 decimals. */
export function formatCents(cents: number | string | null | undefined): string {
  const n = Number(cents)
  if (!Number.isFinite(n)) return '0.00'
  return (n / 100).toFixed(2)
}

/** Parse yuan input (number or string) to integer cents. */
export function yuanToCents(yuan: number | string | null | undefined): number {
  const n = Number(yuan)
  if (!Number.isFinite(n)) return 0
  return Math.round(n * 100)
}

/** Display helper: cents → yuan number for input controls. */
export function centsToYuan(cents: number | string | null | undefined): number {
  const n = Number(cents)
  if (!Number.isFinite(n)) return 0
  return n / 100
}
