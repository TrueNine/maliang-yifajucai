export const HttpMethod_CONSTANTS = [
  'GET', 
  'POST', 
  'PUT', 
  'DELETE', 
  'PATCH', 
  'HEAD', 
  'OPTIONS', 
  'TRACE', 
  'CONNECT'
] as const;
export type HttpMethod = typeof HttpMethod_CONSTANTS[number];
