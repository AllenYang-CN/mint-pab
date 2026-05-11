import request from './request'

export function getBudgets(params) {
  return request({
    url: '/api/budgets',
    method: 'get',
    params
  })
}

export function createBudget(data) {
  return request({
    url: '/api/budgets',
    method: 'post',
    data
  })
}

export function updateBudget(id, data) {
  return request({
    url: `/api/budgets/${id}`,
    method: 'put',
    data
  })
}

export function deleteBudget(id) {
  return request({
    url: `/api/budgets/${id}`,
    method: 'delete'
  })
}

export function getBudgetExecution(params) {
  return request({
    url: '/api/budgets/execution',
    method: 'get',
    params
  })
}
