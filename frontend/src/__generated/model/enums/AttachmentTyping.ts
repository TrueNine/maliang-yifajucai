export const AttachmentTyping_CONSTANTS = [
  'ATTACHMENT', 
  'BASE_URL'
] as const;
export type AttachmentTyping = typeof AttachmentTyping_CONSTANTS[number];
