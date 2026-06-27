import request from '@/utils/request'

/**
 * 获取最新日志
 * @param {number} lines 行数
 * @param {string} level 日志级别
 * @returns {*}
 */
export function getRecentLogs(lines = 100, level = 'ALL') {
  return request({
    url: '/v1/logs/recent',
    method: 'get',
    params: { lines, level }
  })
}

/**
 * 下载日志文件
 * @param {string} level 日志级别
 * @returns {*}
 */
export function downloadLogFile(level = 'ALL') {
  return request({
    url: '/v1/logs/download',
    method: 'get',
    params: { level },
    responseType: 'blob'
  })
}

/**
 * 获取日志文件列表
 * @returns {*}
 */
export function getLogFiles() {
  return request({
    url: '/v1/logs/files',
    method: 'get'
  })
}