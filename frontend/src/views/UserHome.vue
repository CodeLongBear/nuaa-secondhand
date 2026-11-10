<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserDetail, getUserGoods, isFollow, follow, resolveImage } from '../api'
import { coverClass } from '../utils/cover'

const route = useRoute()
const router = useRouter()
const user = ref(null)
const goodsList = ref([])
const followed = ref(false)

async function load() {
  const id = route.params.id
  const [u, g, f] = await Promise.all([getUserDetail(id), getUserGoods(id), isFollow(id)])
  user.value = u.data
  goodsList.value = g.data || []
  followed.value = !!f.data
}

async function toggleFollow() {
  await follow(user.value.id, !followed.value)
  ElMessage.success(followed.value ? '已取消关注' : '已关注')
  followed.value = !followed.value
}

function goDetail(id) {
  router.push(`/goods/${id}`)
}

onMounted(load)
</script>

<template>
  <div v-if="user">
    <el-card style="margin-bottom: 20px">
      <div class="head">
        <div class="avatar">{{ (user.nickname || '?').charAt(0) }}</div>
        <div class="info">
          <h2>{{ user.nickname }}</h2>
          <p class="sub">{{ user.school || '未知学院' }} · 信誉分 {{ user.credit || 0 }}</p>
        </div>
        <el-button :type="followed ? 'default' : 'primary'" @click="toggleFollow">
          {{ followed ? '已关注' : '关注 TA' }}
        </el-button>
      </div>
    </el-card>

    <h3 style="margin-bottom: 16px">TA 的在售商品</h3>
    <el-row :gutter="16">
      <el-col v-for="g in goodsList" :key="g.id" :span="6" style="margin-bottom: 16px">
        <el-card class="goods-card" @click="goDetail(g.id)">
          <img v-if="g.images" :src="resolveImage(g.images.split(',')[0])" class="cover" />
          <div v-else class="cover cover-ph" :class="coverClass(g.category)">
            <span class="cat-name">{{ g.category }}</span>
            <span class="cat-sub">校园二手</span>
          </div>
          <h4 class="t">{{ g.title }}</h4>
          <div class="gm">
            <span class="price num">￥{{ g.price }}</span>
            <el-tag size="small" type="info">{{ g.category }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!goodsList.length" description="TA 还没有在售商品" />
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  gap: 16px;
}
.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--accent);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex: none;
}
.info {
  flex: 1;
}
.sub {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}
.cover {
  width: 100%;
  height: 130px;
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
.goods-card {
  cursor: pointer;
  height: 100%;
}
.t {
  font-size: 14px;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.gm {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.price {
  font-size: 17px;
  font-weight: 700;
}
</style>
