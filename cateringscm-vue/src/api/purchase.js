import request from '@/utils/request'

export function getPurchasePage(params) {
    return request({ url: '/purchase_order/page', method: 'get', params })
}
export const submitPurchase=(data) =>{
    return request({ url: '/purchase_order/submit', method: 'post', data })
}
export const auditPurchase=(id) =>{
    return request({ url: `/purchase_order/auditPass/${id}`, method: 'post' })
}
export const rejectPurchase = (data)=> {
    return request({ url: '/purchase_order/reject', method: 'post', data })
}
export const payPurchaseOrder = (id) => {
    return request({ url: `/purchase_order/pay/${id}`, method: 'post' })
}
export const inboundPurchaseOrder = (id) => {
    return request({ url: `/purchase_order/inbound/${id}`, method: 'post' })
}
export const quickPurchase = (data) => {
    return request({ url: '/purchase_order/quick', method: 'post', data })
}