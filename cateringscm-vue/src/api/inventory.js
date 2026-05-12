import request from '@/utils/request'

export function getInventoryPage(params) {
    return request({ url: '/inventory/page', method: 'get', params })
}
// 后厨领料出库
export function outboundInventory(data) {
    return request({ url: '/inventory/outbound', method: 'post', data })
}
// 月底盘点调整
export function adjustInventory(data) {
    return request({ url: '/inventory/adjust', method: 'post', data })
}
// 查询指定仓库+食材的库存量
export function getStockQty(params) {
    return request({ url: '/inventory/stockQty', method: 'get', params })
}
