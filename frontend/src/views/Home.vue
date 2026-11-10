<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getGoodsList, getSearchHot, getSearchHistory, resolveImage } from '../api'
import { coverClass } from '../utils/cover'

const router = useRouter()
const goodsList = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const keyword = ref('')
const category = ref('')
const sort = ref('')
const priceMin = ref(null)
const priceMax = ref(null)
const condition = ref('')
const hotWords = ref([])
const historyWords = ref([])
const categories = ['数码', '书籍', '生活用品', '服饰', '运动', '其他']
const conditions = ['全新', '九成新', '八成新', '七成新及以下']

async function load() {
  const res = await getGoodsList({
    page: page.value,
    size: size.value,
    keyword: keyword.value,
    category: category.value,
    sort: sort.value,
    priceMin: priceMin.value,
    priceMax: priceMax.value,
    condition: condition.value
  })
  goodsList.value = res.data
  total.value = res.total
}

async function loadHot() {
  try {
    const res = await getSearchHot()
    hotWords.value = res.data || []
  } catch (e) {}
}

async function loadHistory() {
  try {
    const res = await getSearchHistory()
    historyWords.value = res.data || []
  } catch (e) {}
}

function search() {
  page.value = 1
  load()
  loadHistory()
  loadHot()
}

function searchByWord(w) {
  keyword.value = w
  search()
}

function goDetail(id) {
  router.push(`/goods/${id}`)
}

onMounted(() => {
  load()
  loadHot()
  loadHistory()
})
</script>

<template>
  <div>
    <div class="page-head">
      <h1>校园二手交易</h1>
      <p class="page-sub">让闲置流动起来，在校园里发现值得买的好物</p>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品标题 / 描述"
        clearable
        @keyup.enter="search"
        style="width: 280px"
      />
      <el-select v-model="category" placeholder="全部分类" clearable style="width: 130px" @change="search">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-select v-model="condition" placeholder="成色" clearable style="width: 120px" @change="search">
        <el-option v-for="c in conditions" :key="c" :label="c" :value="c" />
      </el-select>
      <el-select v-model="sort" placeholder="排序" clearable style="width: 150px" @change="search">
        <el-option label="最新发布" value="" />
        <el-option label="价格从低到高" value="price" />
        <el-option label="价格从高到低" value="price_desc" />
      </el-select>
      <el-input-number v-model="priceMin" :min="0" placeholder="最低价" style="width: 110px" @change="search" />
      <el-input-number v-model="priceMax" :min="0" placeholder="最高价" style="width: 110px" @change="search" />
      <el-button type="primary" @click="search">搜索</el-button>
    </div>

    <div v-if="hotWords.length || historyWords.length" class="word-bar">
      <template v-if="hotWords.length">
        <span class="word-label">热门</span>
        <el-tag v-for="w in hotWords" :key="'h' + w" class="word-tag" @click="searchByWord(w)">{{ w }}</el-tag>
      </template>
      <template v-if="historyWords.length">
        <span class="word-label">历史</span>
        <el-tag v-for="w in historyWords" :key="'s' + w" type="info" class="word-tag" @click="searchByWord(w)">{{ w }}</el-tag>
      </template>
    </div>

    <el-row :gutter="16">
      <el-col v-for="g in goodsList" :key="g.id" :span="6" style="margin-bottom: 16px">
        <el-card class="goods-card" @click="goDetail(g.id)">
          <img v-if="g.images" :src="resolveImage(g.images.split(',')[0])" class="cover" />
          <div v-else class="cover cover-ph" :class="coverClass(g.category)">
            <span class="cat-name">{{ g.category }}</span>
            <span class="cat-sub">校园二手</span>
          </div>
          <h3 class="goods-title">{{ g.title }}</h3>
          <p class="goods-desc">{{ g.description }}</p>
          <div class="goods-meta">
            <span class="price num">￥{{ g.price }}</span>
            <el-tag v-if="g.isHot === 1" type="danger" size="small">抢购</el-tag>
            <span class="location">{{ g.location }}</span>
          </div>
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
  margin-bottom: 12px;
  flex-wrap: wrap;
  align-items: center;
}
.word-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.word-label {
  font-size: 13px;
  color: var(--muted);
}
.word-tag {
  cursor: pointer;
}
.cover {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 12px;
}
.goods-card {
  cursor: pointer;
  height: 100%;
}
.goods-title {
  font-size: 16px;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.goods-desc {
  color: var(--muted);
  font-size: 13px;
  height: 38px;
  overflow: hidden;
  margin-bottom: 12px;
  line-height: 1.5;
}
.goods-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}
.price {
  color: var(--text);
  font-size: 20px;
  font-weight: 700;
}
.location {
  color: var(--muted);
  font-size: 12px;
  margin-left: auto;
}
</style>
