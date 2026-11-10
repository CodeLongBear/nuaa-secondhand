<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { sign, getSignCount } from '../api'

const count = ref(0)

async function load() {
  const res = await getSignCount()
  count.value = res.data
}

async function handleSign() {
  await sign()
  ElMessage.success('签到成功')
  load()
}

onMounted(load)
</script>

<template>
  <div class="sign-wrap">
    <h2 style="margin-bottom: 28px">每日签到</h2>
    <p class="count num">{{ count }} <span class="unit">天</span></p>
    <p class="hint">已连续签到</p>
    <el-button type="primary" size="large" @click="handleSign">立即签到</el-button>
  </div>
</template>

<style scoped>
.sign-wrap {
  max-width: 420px;
  margin: 40px auto;
  text-align: center;
}
.count {
  font-size: 32px;
  font-weight: 600;
  color: var(--accent);
  margin-bottom: 4px;
}
.unit {
  font-size: 16px;
  font-weight: 400;
  color: var(--muted);
}
.hint {
  color: var(--muted);
  margin-bottom: 24px;
}
</style>
