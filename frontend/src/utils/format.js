export function formatMoney(amount) {
  if (amount === null || amount === undefined || amount === '') {
    return '0.00'
  }
  const num = parseFloat(amount)
  if (isNaN(num)) {
    return '0.00'
  }
  return num.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

export function formatDate(date, format = 'yyyy-MM-dd') {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('yyyy', String(year))
    .replace('MM', month)
    .replace('dd', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

export function formatDateTime(dateTime) {
  return formatDate(dateTime, 'yyyy-MM-dd HH:mm')
}
