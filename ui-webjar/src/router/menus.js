const routes = [
  {
    path: '/mqtt',
    id: 1000,
    name: 'mqtt',
    meta: {
      title: '消息记录',
      icon: 'Connection',
    },
    children: [         {
      path: 'mqtt',
      id: 10001,
      name: 'mqttMonitor',
      meta: {
        title: 'MQTT监控',
        icon: 'Connection',
      }
    }]
  },
  {
    path: '/d',
    id: 2000,
    name: 'd',
    meta: {
      title: '消息记录',
      icon: 'Message',
    },
    children: [      {
      path: 'messages',
      id: 2001,
      name: 'messageRecord',
      meta: {
        title: '消息记录',
        icon: 'Message',
      }
    }]
  },
  {
    path: '/bridge',
    id: 4000,
    name: 'bridge',
    meta: {
      title: '桥接管理',
      icon: 'Share',
    },
    children: [{
      path: 'list',
      id: 4001,
      name: 'bridgeList',
      meta: {
        title: '桥接链路',
        icon: 'Share',
      }
    }]
  },
  {
    path: '/client',
    id: 5000,
    name: 'client',
    meta: {
      title: '客户端管理',
      icon: 'Monitor',
    },
    children: [{
      path: 'list',
      id: 5001,
      name: 'clientList',
      meta: {
        title: '客户端管理',
        icon: 'Monitor',
      }
    }]
  },
  {
    path: '/tool',
    id: 9000,
    name: 'tool',
    meta: {
      title: '工具',
      icon: 'Tools',
    },
    children: [{
      path: 'mqtt',
      id: 9002,
      name: 'mqttTool',
      meta: {
        title: 'MQTT调试',
        icon: 'Connection',
      }
    }]
  },

  {
    path: '/monitor',
    id: 10000,
    name: 'monitor',
    meta: {
      title: '系统监控',
      icon: 'Monitor',
    },
    children: [{
      path: 'actuator',
      id: 10001,
      name: 'actuator',
      meta: {
        title: '监控面板',
        icon: 'DataAnalysis',
      }
    }]
  },
  {
    path: '/log',
    id: 8800,
    name: 'log',
    meta: {
      title: '日志管理',
      icon: 'Tickets',
    },
    children: [{
      path: 'search',
      id: 8803,
      name: 'logSearch',
      meta: {
        title: '日志搜索',
        icon: 'Search',
      }
    },
    {
      path: 'live',
      id: 8804,
      name: 'liveLog',
      meta: {
        title: '实时日志',
        icon: 'Link',
      }
    }]
  },
  {
    path: '/system',
    id: 3000,
    name: 'system',
    meta: {
      title: '系统管理',
      icon: 'UploadFilled',
    },
    children: [{
      path: 'upgrade',
      id: 3002,
      name: 'upgrade',
      meta: {
        title: '系统升级',
        icon: 'UploadFilled',
      }
    }]
  },


]

// 模拟获取动态路由数据
export function getDynamicRoutes() {
  return new Promise((resolve) => {
    resolve(routes.filter(item => {

      if (item.name == 'u') {

        let {role} = JSON.parse(localStorage.getItem("user")||"{\"role\":\"ADMIN\"}");
        if (role == 'ADMIN') {
          return true;
        }else {
          return false;
        }
      }else {
        return true;
      }

    }))
  })
}
