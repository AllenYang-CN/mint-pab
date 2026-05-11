import request from './request'

export function getBalanceSheet(params) {
  return request({
    url: '/api/reports/balance-sheet',
    method: 'get',
    params
  })
}

export function getIncomeExpense(params) {
  return request({
    url: '/api/reports/income-expense',
    method: 'get',
    params
  })
}

export function getCashFlow(params) {
  return request({
    url: '/api/reports/cash-flow',
    method: 'get',
    params
  })
}
