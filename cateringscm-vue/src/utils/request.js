import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
    baseURL: '/api', // 你的后端本地地址
    timeout: 10000
})

// 请求拦截器：自动在请求头带上 token
service.interceptors.request.use(
    config => {
        // 从 localStorage 获取 token
        const token = localStorage.getItem('scm_token')
        if (token) {
            // 这里的 header 名字需与你后端 jwtProperties 中的 tokenName 保持一致，通常是 'token'
            config.headers['token'] = token
        }
        return config
    },
    error => Promise.reject(error)
)

// 响应拦截器：统一处理后端的 Result<T> 格式
// 响应拦截器：统一处理后端的 Result 格式
service.interceptors.response.use(
    response => {
        const res = response.data

        // 💡 打印看看后端到底返回了什么，方便你排查问题
        console.log('后端返回的 Result:', res)

        // 兼容各种常见的后端成功状态码 (用 == 忽略字符串和数字的差异)
        if (res.code == 1 || res.code == 200 || res.code == 0) {
            return res.data // 成功，放行真实数据
        } else {
            // 业务错误提示
            ElMessage.error(res.msg || '操作失败')
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    },
    error => {
        // 处理 401 Token 过期或未登录
        if (error.response && error.response.status === 401) {
            ElMessage.error('登录状态已过期，请重新登录')
            localStorage.removeItem('scm_token')
            router.push('/login')
        } else {
            ElMessage.error(error.message || '网络异常')
        }
        return Promise.reject(error)
    }
)

export default service