export const DisTyping_CONSTANTS = [
  'EYE',
  'EAR',
  'MOUTH',
  'BODY',
  'IQ',
  'NERVE',
  'MULTIPLE',
] as const
export type DisTyping = typeof DisTyping_CONSTANTS[number]
