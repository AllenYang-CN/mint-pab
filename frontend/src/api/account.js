import request from './request'

export function getAccounts(params) {
  return request({
    url: '/api/accounts',
    method: 'get',
    params
  })
}

export function createAccount(data) {
  return request({
    url: '/api/accounts',
    method: 'post',
    data
  })
}

export function updateAccount(id, data) {
  return request({
    url: `/api/accounts/${id}`,
    method: 'put',
    data
  })
}

export function deleteAccount(id) {
  return request({
    url: `/api/accounts/${id}`,
    method: 'delete'
  })
}

export function updateAccountStatus(id, data) {
  return request({
    url: `/api/accounts/${id}/status`,
    method: 'put',
    data
  })
}
