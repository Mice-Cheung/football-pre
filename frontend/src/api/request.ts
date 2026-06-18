import axios from 'axios'

/**
 * API 地址读取优先级：
 *   1. 运行时配置（public/config.js，部署后可修改）
 *   2. 构建时注入的环境变量（VITE_API_BASE_URL）
 *   3. 兜底默认值
 */
const getApiBaseUrl = (): string => {
  if (window.__APP_CONFIG__?.API_BASE_URL) {
    return window.__APP_CONFIG__.API_BASE_URL
  }
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }
  return 'http://localhost:8080/api'
}

const request = axios.create({
  baseURL: getApiBaseUrl(),
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    console.error('API Error:', error.message)
    return Promise.reject(error)
  }
)

export default request
