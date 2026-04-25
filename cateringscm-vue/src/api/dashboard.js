import request from '@/utils/request'

export function getDashboardStat() {
return request({ url: '/dashboard/stat', method: 'get' })
}