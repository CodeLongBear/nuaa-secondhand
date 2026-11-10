<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getGoodsList } from '../api'

const router = useRouter()
const goodsList = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const keyword = ref('')
const category = ref('')
const categories = ['数码', '书籍', '生活用品', '服饰', '运动', '其他']

async function load() {
  const res = await getGoodsList({
    page: page.value,
    size: size.value,
    keyword: keyword.value,
    category: category.value
  })
  goodsList.value = res.data
  total.value = res.total
}

function search() {
  page.value = 1
  load()
}

function goDetail(id) {
  router.push(`/goods/${id}`)
}

onMounted(load)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品标题 / 描述"
        clearable
        @keyup.enter="search"
        style="width: 300px"
      />
      <el-select v-model="category" placeholder="全部分类" clearable style="width: 140px" @change="search">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
    </div>

    <el-row :gutter="16">
      <el-col v-for="g in goodsList" :key="g.id" :span="6" style="margin-bottom: 16px">
        <el-card class="goods-card" @click="goDetail(g.id)">
          <h3 class="goods-title">{{ g.title }}</h3>
          <p class="goods-desc">{{ g.description }}</p>
          <div class="goods-meta">
            <span class="price num">￥{{ g.price }}</span>
            <el-tag v-if="g.isHot === 1" type="danger" size="small">抢购</el-tag>
            <el-tag size="small" type="info">{{ g.category }}</el-tag>
          </div>
          <p class="location">{{ g.location }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!goodsList.length" description="暂无商品" />

    <el-pagination
      v-if="total > size"
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      layout="prev, pager, next, total"
      @current-change="load"
      style="margin-top: 16px; justify-content: center"
    />
  </div>
</template>

<style scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.goods-card {
  cursor: pointer;
  height: 100%;
}
.goods-title {
  font-size: 16px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.goods-desc {
  color: #909399;
  font-size: 13px;
  height: 40px;
  overflow: hidden;
  margin-bottom: 12px;
}
.goods-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.price {
  color: var(--text);
  font-size: 18px;
  font-weight: 700;
}
.location {
  color: #909399;
  font-size: 12px;
}
</style>
