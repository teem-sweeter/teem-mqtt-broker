import request from '@/utils/request'

// ================= Device Credentials API =================

export function getCredentials() {
  return request({
    url: '/v1/security/credentials',
    method: 'get'
  })
}

export function createCredential(data) {
  return request({
    url: '/v1/security/credentials',
    method: 'post',
    data
  })
}

export function updateCredential(id, data) {
  return request({
    url: `/v1/security/credentials/${id}`,
    method: 'put',
    data
  })
}

export function deleteCredential(id) {
  return request({
    url: `/v1/security/credentials/${id}`,
    method: 'delete'
  })
}

export function toggleCredential(id, enable) {
  return request({
    url: `/v1/security/credentials/${id}/toggle`,
    method: 'post',
    params: { enable }
  })
}

// ================= ACL Rules API =================

export function getAclRules() {
  return request({
    url: '/v1/security/acl-rules',
    method: 'get'
  })
}

export function createAclRule(data) {
  return request({
    url: '/v1/security/acl-rules',
    method: 'post',
    data
  })
}

export function updateAclRule(id, data) {
  return request({
    url: `/v1/security/acl-rules/${id}`,
    method: 'put',
    data
  })
}

export function deleteAclRule(id) {
  return request({
    url: `/v1/security/acl-rules/${id}`,
    method: 'delete'
  })
}
