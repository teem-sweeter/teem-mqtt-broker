import request from '@/utils/request'

/**
 * 查询用户分页列表
 * @param {object} query 查询参数
 * @returns {*}
 */
export function findPageList(query) {
  return request({
    url: '/v1/user/findPageList',
    method: 'post',
    data: query
  })
}

/**
 * 保存用户
 * @param {object} data 用户数据
 * @returns {*}
 */
export function save(data) {
  return request({
    url: '/v1/user/save',
    method: 'post',
    data: data
  })
}

/**
 * 更新密码
 * @param {object} data 密码数据
 * @returns {*}
 */
export function updatePassword(data) {
  return request({
    url: '/v1/user/updatePassword',
    method: 'post',
    data: data
  })
}

/**
 * 修改密码
 * @param {object} data 密码数据
 * @returns {*}
 */
export function changePassword(data) {
  return request({
    url: '/v1/user/changePassword',
    method: 'post',
    data: data
  })
}

/**
 * 删除用户
 * @param {number} id 用户ID
 * @returns {*}
 */
export function deleteUser(id) {
  return request({
    url: `/v1/user/${id}`,
    method: 'delete',
  })
}
