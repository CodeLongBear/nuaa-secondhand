import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 30000
})

// 请求拦截器：自动携带 token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.authorization = token
  }
  return config
})

// 响应拦截器：统一处理后端 Result 结构
request.interceptors.response.use(
  (res) => {
    const data = res.data
    if (data && data.success === false) {
      ElMessage.error(data.errorMsg || '操作失败')
      return Promise.reject(new Error(data.errorMsg || '操作失败'))
    }
    return data
  },
  (err) => {
    if (err.response && err.response.status === 401) {
      ElMessage.error('请先登录')
      localStorage.removeItem('token')
      window.location.href = '/login'
    } else {
      ElMessage.error('网络请求失败')
    }
    return Promise.reject(err)
  }
)

export default request
