import request from '@/utils/request'
export const getWarehouseList = () => { return request({ url: '/warehouse/list', method: 'get' }) }
export const addWarehouse = (data) => { return request({ url: '/warehouse', method: 'post', data }) }
export const updateWarehouse = (data) => { return request({ url: '/warehouse/update', method: 'put', data }) }