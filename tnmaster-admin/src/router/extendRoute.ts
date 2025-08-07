import type { IRouteChild } from './RouteInfo'
import { RouteInfo } from './RouteInfo'

interface RouteWithMeta {
  fullPath: string
  name: string
  addToMeta: (meta: Record<string, unknown>) => void
}

export async function extendRoute(route: RouteWithMeta) {
  const pathSegments = route.fullPath.split('/').filter(Boolean)
  if (pathSegments.length === 0) {
    pathSegments.push('/')
  }
  let currentInfo: IRouteChild | undefined = RouteInfo[pathSegments[0]]
  let title = ''
  let requireLogin: boolean | undefined
  let permissions: string[] | undefined
  let hidden: boolean | undefined
  let roles: string[] | undefined
  let showBottomBar: boolean | undefined

  for (let i = 0; i < pathSegments.length; i++) {
    if (i === 0) {
      currentInfo = RouteInfo[pathSegments[i]]
    } else if (currentInfo?.children) {
      currentInfo = currentInfo.children[pathSegments[i]]
    } else {
      break
    }

    if (currentInfo?.title) {
      title = currentInfo.title
    }

    if (currentInfo?.requireLogin !== void 0) {
      requireLogin = currentInfo.requireLogin
    }

    if (currentInfo?.permissions) {
      permissions = currentInfo.permissions
    }

    if (currentInfo?.roles) {
      roles = currentInfo.roles
    }

    if (currentInfo?.hidden !== void 0) {
      hidden = currentInfo.hidden
    }

    if (currentInfo?.showBottomBar !== void 0) {
      showBottomBar = currentInfo.showBottomBar
    }
  }

  const meta = {
    title: title || route.name,
    requireLogin,
    permissions,
    roles,
    hidden,
    showBottomBar,
  }
  route.addToMeta(meta)
}
