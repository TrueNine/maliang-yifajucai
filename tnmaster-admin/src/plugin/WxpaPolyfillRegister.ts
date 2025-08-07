let loaded = false

async function fetchPolyfillList() {
  if (loaded) {
    return
  }
  // eslint-disable-next-line ts/ban-ts-comment
  // @ts-ignore
  await import('core-js/modules/es.object.has-own.js')
  // eslint-disable-next-line ts/ban-ts-comment
  // @ts-ignore
  await import('core-js/modules/es.array.at.js')
  // eslint-disable-next-line ts/ban-ts-comment
  // @ts-ignore
  await import('core-js/modules/es.string.replace-all.js')
  // eslint-disable-next-line ts/ban-ts-comment
  // @ts-ignore
  await import('core-js/modules/es.array.to-sorted.js')
  loaded = true
}

export function registerWxpaPolyfill() {
  if (loaded) {
    return
  }
  const ua = navigator.userAgent.toLowerCase()

  const isWeixin = ua.includes('micromessenger')
  if (isWeixin) {
    void fetchPolyfillList()
  }
}

registerWxpaPolyfill()
