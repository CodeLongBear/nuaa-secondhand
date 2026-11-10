<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyFavorites, removeFavorite, resolveImage } from '../api'

const router = useRouter()
const list = ref([])

async function load() {
  const res = await getMyFavorites()
  list.value = res.data || []
}

async function handleRemove(goodsId) {
  await removeFavorite(goodsId)
  ElMessage.success('已取消收藏')
  load()
}

function goDetail(id) {
  router.push(`/goods/${id}`)
}

onMounted(load)
</script>

<template>
  <div>
    <h2 style="margin-bottom: 20px">我的收藏</h2>
    <el-row :gutter="16">
      <el-col v-for="g in list" :key="g.id" :span="6" style="margin-bottom: 16px">
        <el-card class="fav-card">
          <img
            v-if="g.images"
            :src="resolveImage(g.images.split(',')[0])"
            class="cover"
            @click="goDetail(g.id)"
          />
          <div v-else class="cover placeholder">无图</div>
          <h3 class="t" @click="goDetail(g.id)">{{ g.title }}</h3>
          <div class="meta">
            <span class="price num">￥{{ g.price }}</span>
            <el-tag size="small" type="info">{{ g.category }}</el-tag>
          </div>
          <div class="ops">
            <el-button size="small" type="primary" @click="goDetail(g.id)">查看</el-button>
            <el-button size="small" @click="handleRemove(g.id)">取消收藏</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!list.length" description="暂无收藏" />
  </div>
</template>

<style scoped>
.fav-card {
  height: 100%;
}
.cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 8px;
}
.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  background: #f5f7fa;
}
.t {
  font-size: 15px;
  margin: 0 0 8px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.price {
  font-size: 18px;
  font-weight: 700;
}
.ops {
  display: flex;
  gap: 8px;
}
</style>
