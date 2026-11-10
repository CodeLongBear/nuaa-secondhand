<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getGoodsList, BASE_URL } from '../api'

const recommendation = ref('')
const recommending = ref(false)
const goodsList = ref([])
const selectedGoods = ref(null)
const question = ref('')
const answer = ref('')
const consulting = ref(false)

async function loadGoods() {
  const res = await getGoodsList({ page: 1, size: 100 })
  goodsList.value = res.data
}

// 通用 SSE 流式读取：逐 token 回调 onChunk
async function streamText(path, onChunk) {
  try {
    const resp = await fetch(`${BASE_URL}${path}`, {
      headers: { authorization: localStorage.getItem('token') || '' }
    })
    if (!resp.ok || !resp.body) {
      onChunk('AI 服务暂不可用，请稍后再试')
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
          if (data && data !== '[DONE]') onChunk(data)
        }
      }
    }
  } catch (e) {
    onChunk('AI 服务暂不可用，请稍后再试')
  }
}

async function handleRecommend() {
  recommending.value = true
  recommendation.value = ''
  try {
    await streamText('/agent/recommend/stream', (t) => {
      recommendation.value += t
    })
  } finally {
    recommending.value = false
  }
}

async function handleConsult() {
  if (!selectedGoods.value || !question.value) {
    ElMessage.warning('请选择商品并输入问题')
    return
  }
  consulting.value = true
  answer.value = ''
  try {
    await streamText(
      `/agent/consult/stream/${selectedGoods.value}?question=${encodeURIComponent(question.value)}`,
      (t) => {
        answer.value += t
      }
    )
  } finally {
    consulting.value = false
  }
}

onMounted(loadGoods)
</script>

<template>
  <div>
    <h1 style="margin-bottom: 20px">AI 智能助手</h1>

    <el-card style="margin-bottom: 20px">
      <h3>智能商品推荐</h3>
      <p class="tip">基于你的收藏偏好，AI 流式推荐最值得关注的好物</p>
      <el-button type="primary" :loading="recommending" @click="handleRecommend">获取推荐</el-button>
      <el-alert v-if="recommendation" type="success" :closable="false" style="margin-top: 16px">
        <pre style="white-space: pre-wrap; margin: 0; font-family: inherit">{{ recommendation }}</pre>
      </el-alert>
    </el-card>

    <el-card>
      <h3>智能议价咨询</h3>
      <p class="tip">选择商品，问问 AI 价格是否合理（流式输出）</p>
      <div style="display: flex; gap: 12px; margin-bottom: 12px">
        <el-select v-model="selectedGoods" placeholder="选择商品" filterable style="width: 300px">
          <el-option
            v-for="g in goodsList"
            :key="g.id"
            :label="g.title + '（￥' + g.price + '）'"
            :value="g.id"
          />
        </el-select>
        <el-input v-model="question" placeholder="例如：这个价格合理吗？" @keyup.enter="handleConsult" />
        <el-button type="primary" :loading="consulting" @click="handleConsult">咨询</el-button>
      </div>
      <el-alert v-if="answer" type="success" :closable="false" style="margin-top: 12px">
        <pre style="white-space: pre-wrap; margin: 0; font-family: inherit">{{ answer }}</pre>
      </el-alert>
    </el-card>
  </div>
</template>

<style scoped>
.tip {
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}
</style>
