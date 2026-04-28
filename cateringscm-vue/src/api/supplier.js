import request from '@/utils/request'
export const getSupplierList = () => { return request({ url: '/supplier/list', method: 'get' }) }
export const getSupplierPage = (params) => { return request({ url: '/supplier/page', method: 'get', params }) }
export const addSupplier = (data) => { return request({ url: '/supplier/add', method: 'post', data }) }
export const updateSupplier = (data) => { return request({ url: '/supplier/update', method: 'put', data }) }
export const deleteSupplier = (id) => { return request({ url: `/supplier/${id}`, method: 'delete' }) }