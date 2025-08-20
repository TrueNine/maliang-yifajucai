export const AuditTyping_CONSTANTS = [
  'NONE',
  'ASSIGNED',
  'PASS',
  'FAIL',
  'CANCEL',
  'EXPIRED',
  'REJECT',
  'SHADOW_BAN',
] as const
export type AuditTyping = typeof AuditTyping_CONSTANTS[number]
