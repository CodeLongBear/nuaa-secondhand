<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGoodsDetail, createOrder, seckill, consult } from '../api'

const route = useRoute()
const router = useRouter()
const goods = ref(null)
const consulting = ref(false)
const question = ref('')
const answer = ref('')

async function load() {
  const res = await getGoodsDetail(route.params.id)
  goods.value = res.data
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

async function handleConsult() {
  if (!question.value) {
    ElMessage.warning('请输入你想咨询的问题')
    return
  }
  consulting.value = true
  try {
    const res = await consult(goods.value.id, question.value)
    answer.value = res.data
  } finally {
    consulting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-if="goods">
    <el-card>
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
      </div>
      <div class="actions">
        <el-button type="primary" size="large" @click="handleBuy">立即购买</el-button>
        <el-button v-if="goods.isHot === 1" type="danger" size="large" @click="handleSeckill">
          抢购
        </el-button>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <h3>AI 议价助手</h3>
      <p class="tip">问问 AI：这个价格值不值得买？</p>
      <div class="consult-row">
        <el-input v-model="question" placeholder="例如：这台 iPad 卖 1800 值吗？" @keyup.enter="handleConsult" />
        <el-button type="primary" :loading="consulting" @click="handleConsult">咨询</el-button>
      </div>
      <el-alert v-if="answer" :title="answer" type="success" :closable="false" style="margin-top: 16px" />
    </el-card>
  </div>
</template>

<style scoped>
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
</style>
