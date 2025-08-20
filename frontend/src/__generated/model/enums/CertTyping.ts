export const CertTyping_CONSTANTS = [
  'NONE', 
  'ID_CARD', 
  'ID_CARD2', 
  'DISABILITY_CARD', 
  'DISABILITY_CARD2', 
  'DISABILITY_CARD3', 
  'HOUSEHOLD_CARD', 
  'BANK_CARD', 
  'CONTRACT', 
  'BIZ_LICENSE', 
  'TITLE_IMAGE', 
  'PERSONAL_INCOME_TAX_VIDEO'
] as const;
export type CertTyping = typeof CertTyping_CONSTANTS[number];
