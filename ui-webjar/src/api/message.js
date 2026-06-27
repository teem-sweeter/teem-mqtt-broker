import request from '@/utils/request'

/**
 * 查询消息列表
 * @param {object} params 查询参数
 * @returns {*}
 */
export function getMessages(params) {
  return request({
    url: '/v1/messages',
    method: 'get',
    params
  })
}

/**
 * 获取消息详情
 * @param {number} id 消息ID
 * @returns {*}
 */
export function getMessage(id) {
  return request({
    url: `/v1/messages/${id}`,
    method: 'get'
  })
}

/**
 * 获取消息统计
 * @returns {*}
 */
export function getMessageStats() {
  return request({
    url: '/v1/messages/stats',
    method: 'get'
  })
}

/**
 * 获取消息趋势
 * @param {number} minutes 时间范围（分钟）
 * @returns {*}
 */
export function getMessageTrend(minutes = 60) {
  return request({
    url: '/v1/messages/trend',
    method: 'get',
    params: { minutes }
  })
}

/**
 * 保存消息
 * @param {object} data 消息数据
 * @returns {*}
 */
export function saveMessage(data) {
  return request({
    url: '/v1/messages',
    method: 'post',
    data
  })
}

/**
 * 删除消息
 * @param {number} id 消息ID
 * @returns {*}
 */
export function deleteMessage(id) {
  return request({
    url: `/v1/messages/${id}`,
    method: 'delete'
  })
}

/**
 * 清理过期消息
 * @param {number} days 保留天数
 * @returns {*}
 */
export function cleanupMessages(days = 30) {
  return request({
    url: '/v1/messages/cleanup',
    method: 'delete',
    params: { days }
  })
}
