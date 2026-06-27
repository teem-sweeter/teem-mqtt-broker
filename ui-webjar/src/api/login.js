import request from '@/utils/request'

/**
 * 登录
 * @param form
 * @returns {*}
 */
export function login(form) {
  return request({
    url: '/v1/login',
    method: 'post',
    data: form
  })
}

/**
 * 验证登录
 * @returns {*}
 */
export function check() {
  return request({
    url: '/v1/check',
    method: 'get',
    headers: {
      isToken: false
    }
  })
}

/**
 * 账号初始化
 * @param params
 * @returns {*}
 */
export function initialSetup(params) {
  return request({
    url: '/v1/initialSetup',
    method: 'post',
    data:params
  })
}
