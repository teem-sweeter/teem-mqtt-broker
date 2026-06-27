import request from '@/utils/request'

/**
 * 获取健康状态
 * @returns {*}
 */
export function getHealth() {
  return request({
    url: '/v1/health',
    method: 'get'
  })
}

/**
 * 获取MQTT统计信息
 * @returns {*}
 */
export function getMqttStats() {
  return request({
    url: '/v1/monitor/stats',
    method: 'get'
  })
}

/**
 * 获取MQTT客户端列表
 * @returns {*}
 */
export function getMqttClients() {
  return request({
    url: '/v1/monitor/clients',
    method: 'get'
  })
}

/**
 * 获取单个MQTT客户端详情
 * @param {string} clientId 客户端ID
 * @returns {*}
 */
export function getMqttClient(clientId) {
  return request({
    url: `/v1/monitor/clients/${clientId}`,
    method: 'get'
  })
}

/**
 * 获取MQTT消息列表
 * @param {number} limit 返回数量
 * @returns {*}
 */
export function getMqttMessages(limit = 100) {
  return request({
    url: '/v1/monitor/messages',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取MQTT主题列表
 * @returns {*}
 */
export function getMqttTopics() {
  return request({
    url: '/v1/monitor/topics',
    method: 'get'
  })
}

/**
 * 断开MQTT客户端连接
 * @param {string} clientId 客户端ID
 * @returns {*}
 */
export function disconnectMqttClient(clientId) {
  return request({
    url: `/v1/monitor/clients/${clientId}/disconnect`,
    method: 'post'
  })
}

/**
 * 清除MQTT统计数据
 * @returns {*}
 */
export function clearMqttStats() {
  return request({
    url: '/v1/monitor/stats',
    method: 'delete'
  })
}

/**
 * 发布MQTT消息
 * @param {string} topic 主题
 * @param {number} qos QoS等级
 * @param {string} payload 消息内容
 * @returns {*}
 */
export function publishMqttMessage(topic, qos, payload) {
  return request({
    url: '/v1/monitor/publish',
    method: 'post',
    data: { topic, qos, payload }
  })
}
