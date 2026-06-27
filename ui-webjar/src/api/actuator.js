import request from '@/utils/request'

// Spring Boot Actuator 接口
// 提供对应用健康状况、环境信息、线程转储等的监控功能

/**
 * 检查应用健康状态
 * @returns {Promise} 健康检查结果
 */
export function health() {
  return request({
    url: '/actuator/health',
    method: 'get'
  })
}

/**
 * 获取应用信息
 * @returns {Promise} 应用信息数据
 */
export function info() {
  return request({
    url: '/actuator/info',
    method: 'get'
  })
}

/**
 * 获取环境变量信息
 * @returns {Promise} 环境变量数据
 */
export function env() {
  return request({
    url: '/actuator/env',
    method: 'get'
  })
}

/**
 * 获取线程转储信息
 * @returns {Promise} 线程转储数据
 */
export function threaddump() {
  return request({
    url: '/actuator/threaddump',
    method: 'get'
  })
}

/**
 * 获取日志文件内容
 * @returns {Promise} 日志文件内容（Blob格式）
 */
export function logfile() {
  return request({
    url: '/actuator/logfile',
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 获取堆转储文件
 * @returns {Promise} 堆转储文件（Blob格式）
 */
export function heapdump() {
  return request({
    url: '/actuator/heapdump',
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 获取指标信息
 * @param {string} name - 指标名称，可选
 * @param {object} params - 查询参数，可选
 * @returns {Promise} 指标数据
 */
export function metrics(name="", params={}) {
  //当name为空时，请求的url不能以/结尾
  const url = name ? `/actuator/metrics/${name}` : '/actuator/metrics';
  return request({
    url: url,
    method: 'get',
    params: params
  })
}

/**
 * 获取自定义指标信息
 * @returns {Promise} 自定义指标数据
 */
export function customMetrics() {
  return request({
    url: `/actuator/custom`,
    method: 'get'
  })
}

/**
 * 获取映射信息
 * @returns {Promise} 映射数据
 */
export function mappings() {
  return request({
    url: '/actuator/mappings',
    method: 'get'
  })
}

/**
 * 获取配置属性信息
 * @returns {Promise} 配置属性数据
 */
export function configprops() {
  return request({
    url: '/actuator/configprops',
    method: 'get'
  })
}

/**
 * 获取 Beans 信息
 * @returns {Promise} Beans 数据
 */
export function beans() {
  return request({
    url: '/actuator/beans',
    method: 'get'
  })
}

/**
 * 获取缓存信息
 * @returns {Promise} 缓存数据
 */
export function caches() {
  return request({
    url: '/actuator/caches',
    method: 'get'
  })
}

/**
 * 获取定时任务信息
 * @returns {Promise} 定时任务数据
 */
export function scheduledtasks() {
  return request({
    url: '/actuator/scheduledtasks',
    method: 'get'
  })
}

/**
 * 获取条件评估报告
 * @returns {Promise} 条件评估报告数据
 */
export function conditions() {
  return request({
    url: '/actuator/conditions',
    method: 'get'
  })
}

/**
 * 获取应用启动步骤
 * @returns {Promise} 启动步骤数据
 */
export function startup() {
  return request({
    url: '/actuator/startup',
    method: 'get'
  })
}

/**
 * 获取 SBOM 列表
 * @returns {Promise} SBOM 列表数据
 */
export function sbom() {
  return request({
    url: '/actuator/sbom',
    method: 'get'
  })
}

/**
 * 获取指定 SBOM 详情 (JSON 格式下载)
 * @param {string} id - SBOM ID (例如 "application")
 * @returns {Promise} SBOM 详情
 */
export function getSbomById(id) {
  return request({
    url: `/actuator/sbom/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 重启服务
 * @returns {Promise} 重启结果
 */
export function restart() {
  return request({
    url: '/actuator/restart',
    method: 'post'
  })
}

/**
 * 关闭服务
 * @returns {Promise} 关闭结果
 */
export function shutdown() {
  return request({
    url: '/actuator/shutdown',
    method: 'post'
  })
}

/**
 * 获取日志配置信息
 * @returns {Promise} 日志配置数据
 */
export function loggers() {
  return request({
    url: '/actuator/loggers',
    method: 'get'
  })
}

/**
 * 更新日志级别
 * @param {string} loggerName - 日志记录器名称
 * @param {string} level - 日志级别
 * @returns {Promise} 更新结果
 */
export function updateLoggerLevel(loggerName, level) {
  return request({
    url: `/actuator/loggers/${loggerName}`,
    method: 'post',
    data: { configuredLevel: level }
  })
}
