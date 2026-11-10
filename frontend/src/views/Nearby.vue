<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNearby, resolveImage } from '../api'

const router = useRouter()
const goodsList = ref([])
const longitude = ref(118.79)
const latitude = ref(31.9388)
const radius = ref(10)

async function load() {
  const res = await getNearby({
    longitude: longitude.value,
    latitude: latitude.value,
    radius: radius.value
  })
  goodsList.value = res.data
}

onMounted(load)
</script>

<template>
  <div>
    <h2 style="margin-bottom: 20px">附近好物</h2>
    <div class="search-bar">
      <span>经度</span>
      <el-input-number v-model="longitude" :precision="4" :step="0.01" />
      <span>纬度</span>
      <el-input-number v-model="latitude" :precision="4" :step="0.01" />
      <span>半径</span>
      <el-input-number v-model="radius" :min="1" :max="50" />
      <span>公里</span>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-row :gutter="16">
      <el-col v-for="g in goodsList" :key="g.id" :span="6" style="margin-bottom: 16px">
        <el-card style="cursor: pointer" @click="router.push(`/goods/${g.id}`)">
          <img v-if="g.images" :src="resolveImage(g.images.split(',')[0])" class="cover" />
          <h3>{{ g.title }}</h3>
          <p class="desc">{{ g.location }}</p>
          <span class="price num">￥{{ g.price }}</span>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!goodsList.length" description="附近暂无商品" />
  </div>
</template>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.cover {
  width: 100%;
  height: 130px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 8px;
}
.desc {
  color: #909399;
  font-size: 13px;
  margin: 8px 0;
}
.price {
  color: var(--text);
  font-weight: 700;
}
</style>
