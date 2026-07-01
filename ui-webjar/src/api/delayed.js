import request from '@/utils/request'

/**
 * 获取延迟消息列表
 * @param {string} status 状态过滤 (PENDING/ALL)
 * @returns {*}
 */
export function getDelayedMessages(status = 'PENDING') {
  return request({
    url: '/v1/delayed-messages',
    method: 'get',
    params: { status }
  })
}

/**
 * 创建延迟消息
 * @param {object} data { topic, qos, payload, retain, delaySeconds }
 * @returns {*}
 */
export function createDelayedMessage(data) {
  return request({
    url: '/v1/delayed-messages',
    method: 'post',
    data
  })
}

/**
 * 取消延迟消息
 * @param {number} id 消息ID
 * @returns {*}
 */
export function cancelDelayedMessage(id) {
  return request({
    url: `/v1/delayed-messages/${id}`,
    method: 'delete'
  })
}

/**
 * 获取延迟消息统计
 * @returns {*}
 */
export function getDelayedMessageStats() {
  return request({
    url: '/v1/delayed-messages/stats',
    method: 'get'
  })
}
