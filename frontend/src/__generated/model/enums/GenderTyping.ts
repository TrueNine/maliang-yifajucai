export const GenderTyping_CONSTANTS = [
  'MAN', 
  'WOMAN', 
  'UNKNOWN'
] as const;
export type GenderTyping = typeof GenderTyping_CONSTANTS[number];
