import request from '@/utils/request'


export const getMaterialList = () => { return request({ url: '/material/list', method: 'get' }) }

export function getMaterialPage(params) {
    return request({ url: '/material/page', method: 'get', params })
}
export function addMaterial(data) {
    return request({ url: '/material', method: 'post', data })
}
export function updateMaterial(data) {
    return request({ url: '/material', method: 'put', data })
}
export function deleteMaterial(id) {
    return request({ url: `/material/${id}`, method: 'delete' })
}