import type { TemporalUnit } from './'

export interface Duration {
  ZERO: Duration
  units: Array<TemporalUnit>
  positive: boolean
  zero: boolean
  negative: boolean
  seconds: number
  nano: number
}
