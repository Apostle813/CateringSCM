import request from '@/utils/request'

export const getStoreList = () => {
  return request({ url: '/store/list', method: 'get' })
}

export const getStorePage = (params) => {
  return request({ url: '/store/page', method: 'get', params })
}

export const addStore = (data) => {
  return request({ url: '/store/add', method: 'post', data })
}

export const updateStore = (data) => {
  return request({ url: '/store/update', method: 'put', data })
}

export const deleteStore = (id) => {
  return request({ url: `/store/${id}`, method: 'delete' })
}
