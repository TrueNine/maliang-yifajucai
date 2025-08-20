export const CertPointTyping_CONSTANTS = [
  'NONE',
  'HEADS',
  'TAILS',
  'DOUBLE',
  'ALL',
  'ALL_CONTENT',
  'INTACT',
] as const
export type CertPointTyping = typeof CertPointTyping_CONSTANTS[number]
