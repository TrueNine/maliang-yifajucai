export const RelationItemTyping_CONSTANTS = [
  'NONE', 
  'USER', 
  'CUSTOMER', 
  'ENTERPRISE', 
  'EMPLOYEE', 
  'OTHER'
] as const;
export type RelationItemTyping = typeof RelationItemTyping_CONSTANTS[number];
