export const ISO4217_CONSTANTS = [
  'CNY', 
  'HKD', 
  'MOP', 
  'TWD', 
  'EUR', 
  'USD', 
  'GBP', 
  'JPY', 
  'KRW'
] as const;
export type ISO4217 = typeof ISO4217_CONSTANTS[number];
