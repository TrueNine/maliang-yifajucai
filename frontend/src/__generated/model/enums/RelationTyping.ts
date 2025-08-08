export const RelationTyping_CONSTANTS = [
  'NONE', 
  'BENEFICIARIES', 
  'PARTICIPATOR', 
  'WITNESS', 
  'OTHER'
] as const;
export type RelationTyping = typeof RelationTyping_CONSTANTS[number];
