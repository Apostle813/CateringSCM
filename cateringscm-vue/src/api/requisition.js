import request from '@/utils/request'

export function getRequisitionPage(params) {
    return request({ url: '/requisition_order/page', method: 'get', params })
}
export function submitRequisition(data) {
    return request({ url: '/requisition_order/submit', method: 'post', data })
}
export function auditRequisitionOutbound(id) {
    return request({ url: `/requisition_order/auditOutbound/${id}`, method: 'post' })
}
export function payRequisitionOrder(id) {
    return request({ url: `/requisition_order/pay/${id}`, method: 'post' })
}
export function rejectRequisitionOrder(id) {
    return request({ url: `/requisition_order/rejectOutbound/${id}`, method: 'post' })
}
// 查询请购订单明细
export function getRequisitionDetails(orderId) {
    return request({ url: `/requisition_order/details/${orderId}`, method: 'get' })
}
