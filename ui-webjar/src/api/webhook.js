import request from '@/utils/request'

export function getWebhooks() {
  return request({
    url: '/v1/webhooks',
    method: 'get'
  })
}

export function getWebhook(id) {
  return request({
    url: `/v1/webhooks/${id}`,
    method: 'get'
  })
}

export function createWebhook(data) {
  return request({
    url: '/v1/webhooks',
    method: 'post',
    data
  })
}

export function updateWebhook(id, data) {
  return request({
    url: `/v1/webhooks/${id}`,
    method: 'put',
    data
  })
}

export function deleteWebhook(id) {
  return request({
    url: `/v1/webhooks/${id}`,
    method: 'delete'
  })
}

export function toggleWebhook(id, enable) {
  return request({
    url: `/v1/webhooks/${id}/toggle`,
    method: 'post',
    params: { enable }
  })
}
