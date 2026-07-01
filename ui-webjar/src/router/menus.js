const routes = [
  {
    path: '/mqtt',
    id: 1000,
    name: 'mqtt',
    meta: {
      title: 'menu.messageRecord',
      icon: 'Connection',
    },
    children: [         {
      path: 'mqtt',
      id: 10001,
      name: 'mqttMonitor',
      meta: {
        title: 'menu.mqttMonitor',
        icon: 'Connection',
      }
    }]
  },
  {
    path: '/d',
    id: 2000,
    name: 'd',
    meta: {
      title: 'menu.messageRecord',
      icon: 'Message',
    },
    children: [      {
      path: 'messages',
      id: 2001,
      name: 'messageRecord',
      meta: {
        title: 'menu.messageRecord',
        icon: 'Message',
      }
    }]
  },
  {
    path: '/bridge',
    id: 4000,
    name: 'bridge',
    meta: {
      title: 'menu.bridgeManagement',
      icon: 'Share',
    },
    children: [{
      path: 'list',
      id: 4001,
      name: 'bridgeList',
      meta: {
        title: 'menu.bridgeLink',
        icon: 'Share',
      }
    }]
  },
  {
    path: '/client',
    id: 5000,
    name: 'client',
    meta: {
      title: 'menu.clientManagement',
      icon: 'Monitor',
    },
    children: [{
      path: 'list',
      id: 5001,
      name: 'clientList',
      meta: {
        title: 'menu.clientManagement',
        icon: 'Monitor',
      }
    }]
  },
  {
    path: '/tool',
    id: 9000,
    name: 'tool',
    meta: {
      title: 'menu.tools',
      icon: 'Tools',
    },
    children: [{
      path: 'mqtt',
      id: 9002,
      name: 'mqttTool',
      meta: {
        title: 'menu.mqttDebug',
        icon: 'Connection',
      }
    }]
  },

  {
    path: '/monitor',
    id: 10000,
    name: 'monitor',
    meta: {
      title: 'menu.systemMonitor',
      icon: 'Monitor',
    },
    children: [{
      path: 'actuator',
      id: 10001,
      name: 'actuator',
      meta: {
        title: 'menu.monitorPanel',
        icon: 'DataAnalysis',
      }
    }]
  },
  {
    path: '/log',
    id: 8800,
    name: 'log',
    meta: {
      title: 'menu.logManagement',
      icon: 'Tickets',
    },
    children: [{
      path: 'search',
      id: 8803,
      name: 'logSearch',
      meta: {
        title: 'menu.logSearch',
        icon: 'Search',
      }
    },
    {
      path: 'live',
      id: 8804,
      name: 'liveLog',
      meta: {
        title: 'menu.liveLog',
        icon: 'Link',
      }
    }]
  },
  {
    path: '/system',
    id: 3000,
    name: 'system',
    meta: {
      title: 'menu.systemManagement',
      icon: 'UploadFilled',
    },
    children: [{
      path: 'upgrade',
      id: 3002,
      name: 'upgrade',
      meta: {
        title: 'menu.systemUpgrade',
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
