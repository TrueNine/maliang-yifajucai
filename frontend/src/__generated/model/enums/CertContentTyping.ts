export const CertContentTyping_CONSTANTS = [
  'NONE',
  'IMAGE',
  'SCANNED_IMAGE',
  'SCREEN_SHOT',
  'VIDEO',
  'RECORDING',
  'COPYFILE_IMAGE',
  'REMAKE_IMAGE',
  'PROCESSED_SCANNED_IMAGE',
  'PROCESSED_IMAGE',
  'PROCESSED_VIDEO',
  'PROCESSED_AUDIO',
] as const
export type CertContentTyping = typeof CertContentTyping_CONSTANTS[number]
