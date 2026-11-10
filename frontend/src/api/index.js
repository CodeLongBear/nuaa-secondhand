import request from './request'

// ============ 用户 ============
export const sendCode = (studentNo) => request.post('/user/code', null, { params: { studentNo } })
export const login = (data) => request.post('/user/login', data)
export const getMe = () => request.get('/user/me')

// ============ 商品 ============
export const getGoodsDetail = (id) => request.get(`/goods/detail/${id}`)
export const getGoodsList = (params) => request.get('/goods/list', { params })
export const publishGoods = (data) => request.post('/goods/publish', data)
export const updateGoods = (data) => request.put('/goods', data)
export const deleteGoods = (id) => request.delete(`/goods/${id}`)
export const getHotGoods = () => request.get('/goods/hot')
export const getNearby = (params) => request.get('/goods/nearby', { params })

// ============ 订单 ============
export const createOrder = (goodsId) => request.post(`/order/${goodsId}`)
export const seckill = (goodsId) => request.post(`/order/seckill/${goodsId}`)
export const getMyOrders = () => request.get('/order/my')
export const updateOrderStatus = (orderId, status) => request.put(`/order/${orderId}/${status}`)

// ============ 关注 / Feed ============
export const follow = (userId, isFollow) => request.put(`/follow/${userId}/${isFollow}`)
export const isFollow = (userId) => request.get(`/follow/isFollow/${userId}`)
export const getFeed = () => request.get('/follow/feed')

// ============ 签到 ============
export const sign = () => request.post('/sign')
export const getSignCount = () => request.get('/sign/count')

// ============ AI Agent ============
export const recommend = () => request.get('/agent/recommend')
export const consult = (goodsId, question) =>
  request.post(`/agent/consult/${goodsId}`, null, { params: { question } })
