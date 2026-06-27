import request from '@/utils/request'

/**
 * 获取已连接客户端列表（含管控状态）
 * @returns {*}
 */
export function getClients() {
  return request({
    url: '/v1/clients',
    method: 'get'
  })
}

/**
 * 踢出客户端
 * @param {string} clientId 客户端ID
 * @returns {*}
 */
export function kickClient(clientId) {
  return request({
    url: `/v1/clients/${clientId}/kick`,
    method: 'post'
  })
}

/**
 * 设置客户端发送权限
 * @param {string} clientId 客户端ID
 * @param {boolean} disabled 是否禁止发送
 * @returns {*}
 */
export function setSendDisabled(clientId, disabled) {
  return request({
    url: `/v1/clients/${clientId}/send`,
    method: 'post',
    params: { disabled }
  })
}

/**
 * 设置客户端接收权限
 * @param {string} clientId 客户端ID
 * @param {boolean} disabled 是否禁止接收
 * @returns {*}
 */
export function setReceiveDisabled(clientId, disabled) {
  return request({
    url: `/v1/clients/${clientId}/receive`,
    method: 'post',
    params: { disabled }
  })
}
