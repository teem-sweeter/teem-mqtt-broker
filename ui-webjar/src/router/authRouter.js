/**
 * 根据用户菜单权限设置路由
 * @param menuData
 */
import layout from '../layout/index.vue'
import logSearch from '../views/LogPreview/index.vue'
import liveLog from '../views/LiveLog/index.vue'
import upgrade from '../views/Upgrade/index.vue'
import actuator from '../views/Actuator/index.vue'
import mqttMonitor from '../views/Monitor/index.vue'
import mqttTool from '../views/MqttTool/index.vue'
import messageRecord from '../views/MessageRecord/index.vue'
import bridgeList from '../views/Bridge/index.vue'
import clientList from '../views/Client/index.vue'
import webhook from '../views/WebHook/index.vue'
import clientCredentials from '../views/Security/ClientCredentials.vue'
import aclRules from '../views/Security/AclRules.vue'

const componentObj = {
  layout,
  logSearch,
  liveLog,
  upgrade,
  actuator,
  mqttMonitor,
  mqttTool,
  messageRecord,
  bridgeList,
  clientList,
  webhook,
  clientCredentials,
  aclRules
}
/**
 * 动态添加用户路由权限
 * @param menuData json格式的字符串
 * @returns {[]}
 */
export function getAuthRouters(menuData) {
  const routerList = []
  let obj, children
  menuData && menuData.map(item => {
    obj = {
      path: item.path,
      component: layout,
      redirect: 'noRedirect',
      name: item.name,
      meta: {
        breadcrumbName: item.meta.title,
        icon: item.meta.icon,
      }
    }
    // 设置子菜单
    children = []
    if (item['children'] && item['children'].length > 0) {
      item['children'].map(it => {
        children.push({
          path: it.path,
          component: componentObj[it.name],
          // 如果该组件需要缓存,这里的name属性请与组件的name保持一致
          name: it.name,
          meta: {
            breadcrumbName: it.meta.title,
            // 是否需要缓存
            cached: true,
            keepAlive: true,
            icon: it.meta.icon,
            hidden: it.meta.hidden || false,
          }
        })
      });
    }
    obj.children = children
    routerList.push({
      ...obj,
      children: children
    })
  })
  return routerList
}
