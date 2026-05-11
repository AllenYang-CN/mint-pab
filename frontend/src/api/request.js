import axios from 'axios'
import { Message } from 'element-ui'
import { getToken, removeToken } from '@/utils/auth'
import router from '@/router'

const request = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 处理文件流下载
    if (res instanceof Blob) {
      return response
    }
    if (res.code === 200) {
      return res
    }
    if (res.code === 401) {
      removeToken()
      router.push('/login')
      Message.error(res.message || '登录已过期，请重新登录')
      return Promise.reject(new Error(res.message || '登录已过期'))
    }
    Message.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    const { response } = error
    if (response && response.status === 401) {
      removeToken()
      router.push('/login')
      Message.error('登录已过期，请重新登录')
    } else {
      Message.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
