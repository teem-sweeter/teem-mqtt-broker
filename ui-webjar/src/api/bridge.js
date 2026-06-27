import request from '@/utils/request'

/**
 * 获取所有桥接链路
 * @returns {*}
 */
export function getBridges() {
  return request({
    url: '/v1/bridges',
    method: 'get'
  })
}

/**
 * 获取单个桥接链路详情
 * @param {number} id 桥接链路ID
 * @returns {*}
 */
export function getBridge(id) {
  return request({
    url: `/v1/bridges/${id}`,
    method: 'get'
  })
}

/**
 * 创建桥接链路
 * @param {object} data 桥接链路配置
 * @returns {*}
 */
export function createBridge(data) {
  return request({
    url: '/v1/bridges',
    method: 'post',
    data
  })
}

/**
 * 更新桥接链路
 * @param {number} id 桥接链路ID
 * @param {object} data 桥接链路配置
 * @returns {*}
 */
export function updateBridge(id, data) {
  return request({
    url: `/v1/bridges/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除桥接链路
 * @param {number} id 桥接链路ID
 * @returns {*}
 */
export function deleteBridge(id) {
  return request({
    url: `/v1/bridges/${id}`,
    method: 'delete'
  })
}

/**
 * 启用/禁用桥接链路
 * @param {number} id 桥接链路ID
 * @param {boolean} enable 是否启用
 * @returns {*}
 */
export function toggleBridge(id, enable) {
  return request({
    url: `/v1/bridges/${id}/toggle`,
    method: 'post',
    params: { enable }
  })
}

/**
 * 获取桥接链路实时统计
 * @param {number} id 桥接链路ID
 * @returns {*}
 */
export function getBridgeStats(id) {
  return request({
    url: `/v1/bridges/${id}/stats`,
    method: 'get'
  })
}

/**
 * 测试桥接连接
 * @param {object} data 连接配置
 * @returns {*}
 */
export function testBridgeConnection(data) {
  return request({
    url: '/v1/bridges/test-connect',
    method: 'post',
    data
  })
}

/**
 * 获取指定桥接的路由规则列表
 * @param {number} bridgeId 桥接链路ID
 * @returns {*}
 */
export function getRouteRules(bridgeId) {
  return request({
    url: `/v1/bridges/${bridgeId}/routes`,
    method: 'get'
  })
}

/**
 * 批量保存路由规则
 * @param {number} bridgeId 桥接链路ID
 * @param {Array} data 路由规则列表
 * @returns {*}
 */
export function saveRouteRules(bridgeId, data) {
  return request({
    url: `/v1/bridges/${bridgeId}/routes`,
    method: 'put',
    data
  })
}
