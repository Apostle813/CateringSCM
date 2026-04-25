import request from '@/utils/request'

export function getStockLogPage(params) {
    return request({ url: '/stock_log/page', method: 'get', params })
}