import request from '@/utils/request'

export function getPurchasePage(params) {
    return request({ url: '/purchase_order/page', method: 'get', params })
}
export function submitPurchase(data) {
    return request({ url: '/purchase_order/submit', method: 'post', data })
}
export function auditPurchase(id) {
    return request({ url: `/purchase_order/audit/${id}`, method: 'post' })
}
export function rejectPurchase(data) {
    return request({ url: '/purchase_order/reject', method: 'post', data })
}