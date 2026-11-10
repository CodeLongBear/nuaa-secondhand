<script setup>
import { ref, onMounted } from 'vue'
import { getFeed, resolveImage } from '../api'
import { coverClass } from '../utils/cover'

const feeds = ref([])

async function load() {
  const res = await getFeed()
  feeds.value = res.data
}

onMounted(load)
</script>

<template>
  <div>
    <h1 style="margin-bottom: 20px">关注动态</h1>
    <el-timeline v-if="feeds.length">
      <el-timeline-item v-for="f in feeds" :key="f.id" :timestamp="f.createTime">
        <el-card>
          <img v-if="f.images" :src="resolveImage(f.images.split(',')[0])" class="cover" />
          <div v-else class="cover cover-ph" :class="coverClass(f.category)">
            <span class="cat-name" style="font-size: 14px; letter-spacing: 1px">{{ f.category }}</span>
          </div>
          <h3>{{ f.title }}</h3>
          <p class="desc">{{ f.description }}</p>
          <div>
            <span class="price num">￥{{ f.price }}</span>
            <span class="loc">{{ f.location }}</span>
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-if="!feeds.length" description="暂无关注动态，去关注几个卖家吧" />
  </div>
</template>

<style scoped>
.cover {
  width: 120px;
  height: 120px;
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
  margin-right: 12px;
}
.loc {
  color: #909399;
  font-size: 12px;
}
</style>
