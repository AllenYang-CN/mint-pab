import request from './request'

export function createTransaction(data) {
  return request({
    url: '/api/transactions',
    method: 'post',
    data
  })
}

export function updateTransaction(id, data) {
  return request({
    url: `/api/transactions/${id}`,
    method: 'put',
    data
  })
}

export function deleteTransaction(id) {
  return request({
    url: `/api/transactions/${id}`,
    method: 'delete'
  })
}

export function getTransaction(id) {
  return request({
    url: `/api/transactions/${id}`,
    method: 'get'
  })
}

export function getTransactions(params) {
  return request({
    url: '/api/transactions',
    method: 'get',
    params
  })
}

export function exportTransactions(params) {
  return request({
    url: '/api/transactions/export',
    method: 'get',
    params,
    responseType: 'blob'
  }).then((response) => {
    const blob = new Blob([response.data])
    const contentDisposition = response.headers['content-disposition']
    let filename = 'transactions.xlsx'
    if (contentDisposition) {
      const match = contentDisposition.match(/filename="?([^"]+)"?/)
      if (match) {
        filename = decodeURIComponent(match[1])
      }
    }
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)
  })
}
