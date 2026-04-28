import request from '@/utils/request'
export const getWarehouseList = () => { return request({ url: '/warehouse/list', method: 'get' }) }