<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHotGoods, seckill } from '../api'

const goodsList = ref([])

async function load() {
  const res = await getHotGoods()
  goodsList.value = res.data
}

async function handleSeckill(goodsId) {
  await seckill(goodsId)
  ElMessage.success('抢购成功，订单生成中')
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2 style="margin-bottom: 20px">限时抢购</h2>
    <el-row :gutter="16">
      <el-col v-for="g in goodsList" :key="g.id" :span="6" style="margin-bottom: 16px">
        <el-card>
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
