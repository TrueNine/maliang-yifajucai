import type {Duration} from './';

export interface TemporalUnit {
  duration: Duration;
  durationEstimated: boolean;
  dateBased: boolean;
  timeBased: boolean;
}
