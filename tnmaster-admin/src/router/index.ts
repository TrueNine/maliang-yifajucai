import { createRouter, createWebHistory } from 'vue-router'
import { routes as autoRoutes, handleHotUpdate } from 'vue-router/auto-routes'
import { useConfigStore } from '@/store'

const Router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: autoRoutes,
})

if (import.meta.hot) {
  handleHotUpdate(Router)
}

const routes = Router.getRoutes()

Router.beforeEach((to, _, next) => {
  const routeTable = routes.map((i) => i.path)
  const hasToRoute = routeTable.includes(to.path)
  if (to.path.startsWith('/error')) {
    next()
    return
  }
  if (!hasToRoute) {
    next({
      path: `/error`,
      query: { code: 404, msg: '页面不存在', toPath: to.path },
    })
    return
  }
  next()
})

// TODO 闪屏问题
Router.beforeEach((to, _, next) => {
  const cfgStore = useConfigStore()
  if (typeof to.meta?.title === 'string' && to.meta.title) {
    document.title = to.meta.title
  }
  cfgStore.bottomNavigationShow = false
  next()
})

export { autoRoutes as AllRouteTable, Router }
export default Router
