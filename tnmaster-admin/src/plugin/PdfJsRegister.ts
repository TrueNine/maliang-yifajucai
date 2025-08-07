import { init, setWorkerSrc } from '@compose/external/pdfjs-dist'

import u from 'pdfjs-dist/build/pdf.worker.min.mjs?url'

export function registerPdfJs() {
  setWorkerSrc(u)
  init()
}
