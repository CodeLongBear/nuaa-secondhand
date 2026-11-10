<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHotGoods, seckill, resolveImage } from '../api'

const goodsList = ref([])
const now = ref(Date.now())
let timer = null

async function load() {
  const res = await getHotGoods()
  goodsList.value = res.data
}

async function handleSeckill(goodsId) {
  await seckill(goodsId)
  ElMessage.success('抢购成功，订单生成中')
  load()
}

function remaining() {
  const end = new Date()
  end.setHours(23, 59, 59, 999)
  const diff = Math.max(0, end.getTime() - now.value)
  const h = Math.floor(diff / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  return `${h} 时 ${m} 分 ${s} 秒`
}

onMounted(() => {
  load()
  timer = setInterval(() => {
    now.value = Date.now()
  }, 1000)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div>
    <h2 style="margin-bottom: 20px">限时抢购</h2>
    <div class="countdown">
      距离今日抢购结束还有
      <span class="cd-time">{{ remaining() }}</span>
    </div>
    <el-row :gutter="16">
      <el-col v-for="g in goodsList" :key="g.id" :span="6" style="margin-bottom: 16px">
        <el-card>
          <img v-if="g.images" :src="resolveImage(g.images.split(',')[0])" class="cover" />
          <div v-else class="cover placeholder">无图</div>
          <h3>{{ g.title }}</h3>
          <p class="desc">{{ g.description }}</p>
          <div class="meta">
            <span class="price num">￥{{ g.price }}</span>
            <el-tag type="danger">库存 {{ g.stock }}</el-tag>
          </div>
          <el-button type="danger" style="width: 100%; margin-top: 12px" @click="handleSeckill(g.id)">
            立即抢购
          </el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!goodsList.length" description="暂无抢购商品" />
  </div>
</template>

<style scoped>
.countdown {
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
}
.cd-time {
  color: #e5484d;
  font-weight: 700;
  font-size: 16px;
}
.cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 8px;
}
.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  background: #f5f7fa;
}
.desc {
  color: #909399;
  font-size: 13px;
  height: 40px;
  overflow: hidden;
  margin: 8px 0;
}
.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.price {
  color: var(--text);
  font-size: 20px;
  font-weight: 700;
}
</style>
