import type { IsoChronology, TemporalUnit } from './'

export interface Period {
  ZERO: Period
  units: Array<TemporalUnit>
  chronology: IsoChronology
  zero: boolean
  negative: boolean
  years: number
  months: number
  days: number
}
