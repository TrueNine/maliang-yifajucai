export const DegreeTyping_CONSTANTS = [
  'NONE',
  'MIN',
  'HALF',
  'HALF_TECH',
  'HEIGHT',
  'HEIGHT_TECH',
  'BIG',
  'DISCOVERY',
  'EXPERT',
  'AFTER_EXPERT',
  'OTHER',
] as const
export type DegreeTyping = typeof DegreeTyping_CONSTANTS[number]
