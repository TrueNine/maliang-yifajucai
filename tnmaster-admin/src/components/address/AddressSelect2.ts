import type { asyncable, latenil } from '@compose/types'
import type { RemovableRef } from '@vueuse/core'

export interface AddressSelectItem {
  code?: string
  name?: string
  level?: number
}

export function addressSortFn(a?: AddressSelectItem, b?: AddressSelectItem): number {
  if (!a || !b) {
    return 0
  }
  if ((a.code == null) || (b.code == null)) {
    return 0
  }
  return a.code.localeCompare(b.code)
}

/**
 * ## 查询缓存或找寻新地址并将其保存到缓存
 *
 * @param code 地址
 * @param cache 缓存
 * @param findFn 查找新值方法
 * @returns 缓存或新地址信息
 */
export async function getCache(
  code: string,
  cache: RemovableRef<undefined | Record<string, AddressSelectItem[] | undefined>>,
  findFn: (v: string) => asyncable<latenil<AddressSelectItem[]>>,
) {
  if (!cache.value) {
    cache.value = {}
  }
  let cacheData = cache.value[code]
  if (!cacheData) {
    cacheData = cache.value[code] = []
  }
  if (cacheData.length > 0) {
    return cacheData
  } else {
    const f = findFn(code)
    cache.value[code] = ((await f) ?? []).sort(addressSortFn)
    return f
  }
}
