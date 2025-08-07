import { init, setWorkerSrc } from '@compose/external/libarchive-js'
import u from 'libarchive.js/dist/worker-bundle.js?url'

export function registerLibArchiveJs() {
  setWorkerSrc(u)
  init()
}
