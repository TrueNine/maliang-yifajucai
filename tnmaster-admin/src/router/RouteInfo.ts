export interface IRouteChild {
  title: string
  requireLogin?: boolean
  permissions?: string[]
  roles?: string[]
  showBottomBar?: boolean
  children?: Record<string, IRouteChild>
  hidden?: boolean
}

export interface IRouteInfo {
  [key: string]: IRouteChild
}

export const RouteInfo: IRouteInfo = {
  '/': {
    title: '首页',
    requireLogin: false,
    showBottomBar: true,
  },
  'wxpa': {
    title: '微信公众号',
    requireLogin: false,
    showBottomBar: true,
    children: {
      service: {
        title: '服务',
        requireLogin: false,
        showBottomBar: true,
      },
      me: {
        title: '个人信息',
        requireLogin: true,
        showBottomBar: true,
      },
      auth: {
        title: '登录',
        requireLogin: false,
        showBottomBar: false,
      },
    },
  },

  'auth': {
    title: '认证',
    requireLogin: false,
    permissions: [],
    roles: [],
    showBottomBar: false,
  },

  'error': {
    title: '错误',
    requireLogin: false,
    permissions: [],
    roles: [],
    hidden: true,
    showBottomBar: false,
  },

  'a': {
    title: '管理后台',
    requireLogin: true,
    permissions: ['ADMIN'],
    roles: ['ADMIN'],
    showBottomBar: false,
    children: {
      audit: {
        title: '审核',
        children: {
          enterprises: {
            title: '企业审核',
          },
          user_certs: {
            title: '用户证件审核',
          },
        },
      },
      blacklist: {
        title: '黑名单',
        children: {
          add: {
            title: '添加黑名单',
          },
          all: {
            title: '黑名单列表',
          },
        },
      },
      dashboard: {
        title: '首页',
      },
      user_info: {
        title: '用户管理',
        children: {
          add: {
            title: '添加用户',
          },
          all: {
            title: '用户列表',
          },
          details: {
            title: '用户详情',
            hidden: true,
          },
        },
      },
      sys: {
        title: 'DEV_OPS',
        requireLogin: true,
        permissions: ['ROOT'],
        roles: ['ROOT'],
        children: {
          acl: {
            title: '权限管理',
          },
          api: {
            title: 'API管理',
          },
          cache_config: {
            title: '缓存配置',
          },
          menu_admin: {
            title: '菜单管理',
          },
        },
      },
    },
  },
}
