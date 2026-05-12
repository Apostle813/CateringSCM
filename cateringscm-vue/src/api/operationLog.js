import request from '@/utils/request'

export function getOperationLogPage(params) {
    return request({ url: '/operation_log/page', method: 'get', params })
}
