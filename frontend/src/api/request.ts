import axios from 'axios'

// 前后端同机部署，直接调用本地后端 8080 端口
const BASE_URL = 'http://localhost:8080/api'

const request = axios.create({
  baseURL: BASE_URL,
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
