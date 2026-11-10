import request from './request'

export const BASE_URL = 'http://localhost:8081'

// 图片 URL 前缀（后端返回 /uploads/xxx 相对路径）
export const resolveImage = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return BASE_URL + url
}

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
export const getGoodsUv = (id) => request.get(`/goods/uv/${id}`)
export const getSearchHistory = () => request.get('/goods/search/history')
export const getSearchHot = () => request.get('/goods/search/hot')

// ============ 订单 ============
export const createOrder = (goodsId) => request.post(`/order/${goodsId}`)
export const seckill = (goodsId) => request.post(`/order/seckill/${goodsId}`)
export const getMyOrders = () => request.get('/order/my')
export const updateOrderStatus = (orderId, status) => request.put(`/order/${orderId}/${status}`)

// ============ 关注 / Feed ============
export const follow = (userId, isFollow) => request.put(`/follow/${userId}/${isFollow}`)
export const isFollow = (userId) => request.get(`/follow/isFollow/${userId}`)
export const getFeed = () => request.get('/follow/feed')
export const getCommonFollow = (otherId) => request.get(`/follow/common/${otherId}`)

// ============ 签到 ============
export const sign = () => request.post('/sign')
export const getSignCount = () => request.get('/sign/count')

// ============ 评论 ============
export const getComments = (goodsId) => request.get(`/comment/goods/${goodsId}`)
export const addComment = (data) => request.post('/comment', null, { params: data })

// ============ 收藏 ============
export const addFavorite = (goodsId) => request.post(`/favorite/${goodsId}`)
export const removeFavorite = (goodsId) => request.delete(`/favorite/${goodsId}`)
export const isFavorite = (goodsId) => request.get(`/favorite/isFavorite/${goodsId}`)
export const getMyFavorites = () => request.get('/favorite/my')

// ============ 评价 ============
export const createReview = (data) => request.post('/review', null, { params: data })
export const getUserReviews = (userId) => request.get(`/review/user/${userId}`)

// ============ 消息 ============
export const getMessages = (onlyUnread = 0) => request.get('/message/list', { params: { onlyUnread } })
export const getUnreadCount = () => request.get('/message/unreadCount')
export const readMessage = (id) => request.put(`/message/read/${id}`)

// ============ 图片上传 ============
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

// ============ AI Agent ============
export const recommend = () => request.get('/agent/recommend')
export const consult = (goodsId, question) =>
  request.post(`/agent/consult/${goodsId}`, null, { params: { question } })
