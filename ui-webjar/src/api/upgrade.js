import request from '@/utils/request.js'

/**
 * 上传系统升级包
 * @param data
 * @returns {*}
 */
export function uploadUpdatePackage(data) {
  return request({
    url: '/v1/upgrade',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 检查系统健康状态
 * @returns {*}
 */
export function checkSystemHealth() {
  return request({
    url: '/health',
    method: 'get',
    // 即使在更新过程中程序接口无响应，也要返回axios的response给下游处理
    timeout: 5000 // 设置5秒超时
  }).catch(error => {
    // 将错误也返回给下游处理，而不是直接抛出
    return Promise.resolve(error.response || error);
  })
}
