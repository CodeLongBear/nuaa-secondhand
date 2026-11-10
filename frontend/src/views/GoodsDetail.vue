<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getGoodsDetail, createOrder, seckill, addFavorite, removeFavorite, isFavorite,
  getComments, addComment, getGoodsUv, getMe, deleteGoods, resolveImage, BASE_URL,
  follow, isFollow
} from '../api'

const route = useRoute()
const router = useRouter()

const goods = ref(null)
const images = ref([])
const favorited = ref(false)
const sellerFollowed = ref(false)
const uv = ref(0)
const comments = ref([])
const commentText = ref('')
const consulting = ref(false)
const question = ref('')
const answer = ref('')
const currentUserId = ref(null)

const isSeller = computed(() => goods.value && currentUserId.value && goods.value.sellerId === currentUserId.value)

async function load() {
  const res = await getGoodsDetail(route.params.id)
  goods.value = res.data
  images.value = (res.data.images || '').split(',').filter(Boolean).map(resolveImage)
  loadCurrentUser()
  loadUv()
  loadComments()
  loadFavorite()
  loadSellerFollow()
}

async function loadCurrentUser() {
  try {
    const res = await getMe()
    currentUserId.value = res.data?.id
  } catch (e) {}
}

async function loadUv() {
  try {
    const res = await getGoodsUv(route.params.id)
    uv.value = res.data
  } catch (e) {}
}

async function loadFavorite() {
  try {
    const res = await isFavorite(route.params.id)
    favorited.value = !!res.data
  } catch (e) {}
}

async function loadSellerFollow() {
  try {
    if (goods.value?.sellerId) {
      const res = await isFollow(goods.value.sellerId)
      sellerFollowed.value = !!res.data
    }
  } catch (e) {}
}

async function toggleFollowSeller() {
  if (!goods.value?.sellerId) return
  await follow(goods.value.sellerId, !sellerFollowed.value)
  ElMessage.success(sellerFollowed.value ? '已取消关注' : '已关注卖家')
  sellerFollowed.value = !sellerFollowed.value
}

async function loadComments() {
  try {
    const res = await getComments(route.params.id)
    comments.value = res.data || []
  } catch (e) {}
}

async function handleBuy() {
  await createOrder(goods.value.id)
  ElMessage.success('下单成功')
  router.push('/orders')
}

async function handleSeckill() {
  await seckill(goods.value.id)
  ElMessage.success('抢购请求已提交')
}

async function toggleFavorite() {
  if (favorited.value) {
    await removeFavorite(goods.value.id)
    ElMessage.success('已取消收藏')
  } else {
    await addFavorite(goods.value.id)
    ElMessage.success('已收藏')
  }
  favorited.value = !favorited.value
}

async function submitComment() {
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  await addComment({ goodsId: goods.value.id, parentId: 0, content: commentText.value })
  ElMessage.success('评论成功')
  commentText.value = ''
  loadComments()
}

async function handleConsult() {
  if (!question.value) {
    ElMessage.warning('请输入你想咨询的问题')
    return
  }
  consulting.value = true
  answer.value = ''
  try {
    const url = `${BASE_URL}/agent/consult/stream/${goods.value.id}?question=${encodeURIComponent(question.value)}`
    const resp = await fetch(url, { headers: { authorization: localStorage.getItem('token') || '' } })
    if (!resp.ok || !resp.body) {
      answer.value = 'AI 服务暂不可用，请稍后再试'
      return
    }
    const reader = resp.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop()
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim()
          if (data && data !== '[DONE]') answer.value += data
        }
      }
    }
  } catch (e) {
    answer.value = 'AI 服务暂不可用，请稍后再试'
  } finally {
    consulting.value = false
  }
}

async function handleDelete() {
  await ElMessageBox.confirm('确定下架该商品吗？', '提示', { type: 'warning' })
  await deleteGoods(goods.value.id)
  ElMessage.success('已下架')
  router.push('/home')
}

onMounted(load)
</script>

<template>
  <div v-if="goods">
    <el-card>
      <div v-if="images.length" class="gallery">
        <img v-for="(img, i) in images" :key="i" :src="img" class="gallery-img" />
      </div>
      <h2>{{ goods.title }}</h2>
      <el-divider />
      <p class="desc">{{ goods.description }}</p>
      <div class="info-grid">
        <div><span class="label">价格</span><span class="price num">￥{{ goods.price }}</span></div>
        <div><span class="label">分类</span>{{ goods.category }}</div>
        <div><span class="label">成色</span>{{ goods.conditionDesc || '未说明' }}</div>
        <div><span class="label">位置</span>{{ goods.location }}</div>
        <div><span class="label">库存</span>{{ goods.stock }}</div>
        <div><span class="label">浏览量</span>{{ goods.viewCount }}</div>
        <div><span class="label">UV 浏览</span>{{ uv }}</div>
        <div><span class="label">卖家 ID</span>{{ goods.sellerId }}</div>
      </div>
      <div class="actions">
        <el-button type="primary" size="large" @click="handleBuy">立即购买</el-button>
        <el-button v-if="goods.isHot === 1" type="danger" size="large" @click="handleSeckill">抢购</el-button>
        <el-button size="large" :type="favorited ? 'warning' : 'default'" @click="toggleFavorite">
          {{ favorited ? '取消收藏' : '收藏' }}
        </el-button>
        <el-button v-if="!isSeller" size="large" :type="sellerFollowed ? 'default' : 'primary'" @click="toggleFollowSeller">
          {{ sellerFollowed ? '已关注卖家' : '关注卖家' }}
        </el-button>
        <el-button v-if="isSeller" size="large" type="info" @click="handleDelete">下架商品</el-button>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <h3>评论区</h3>
      <div class="comment-input">
        <el-input v-model="commentText" placeholder="说点什么..." @keyup.enter="submitComment" />
        <el-button type="primary" @click="submitComment">发表</el-button>
      </div>
      <el-empty v-if="!comments.length" description="暂无评论" />
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-head">
          <span class="comment-user">{{ c.nickname }}</span>
          <span class="comment-time">{{ c.createTime }}</span>
        </div>
        <div class="comment-content">{{ c.content }}</div>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <h3>AI 议价助手</h3>
      <p class="tip">问问 AI：这个价格值不值得买？（流式输出）</p>
      <div class="consult-row">
        <el-input v-model="question" placeholder="例如：这台 iPad 卖 1800 值吗？" @keyup.enter="handleConsult" />
        <el-button type="primary" :loading="consulting" @click="handleConsult">咨询</el-button>
      </div>
      <el-alert v-if="answer" :title="answer" type="success" :closable="false" style="margin-top: 16px; white-space: pre-wrap" />
    </el-card>
  </div>
</template>

<style scoped>
.gallery {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.gallery-img {
  width: 160px;
  height: 160px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid var(--border);
}
.desc {
  color: #606266;
  margin-bottom: 16px;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.label {
  color: #909399;
  margin-right: 8px;
}
.price {
  color: var(--text);
  font-size: 24px;
  font-weight: 700;
}
.actions {
  display: flex;
  gap: 12px;
}
.tip {
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}
.consult-row {
  display: flex;
  gap: 12px;
}
.comment-input {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}
.comment-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}
.comment-user {
  font-weight: 600;
  color: var(--text);
}
.comment-time {
  font-size: 12px;
  color: #909399;
}
.comment-content {
  color: #606266;
}
</style>
