import request from './request'

export function getDashboardSummary() {
  return request({
    url: '/api/dashboard/summary',
    method: 'get'
  })
}
