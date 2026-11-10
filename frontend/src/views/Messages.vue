<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessages, readMessage } from '../api'

const list = ref([])
const onlyUnread = ref(0)
const typeMap = { 1: '下单', 2: '评论', 3: '关注', 4: '评价' }
const typeTag = { 1: 'primary', 2: 'info', 3: 'success', 4: 'warning' }

async function load() {
  const res = await getMessages(onlyUnread.value)
  list.value = res.data || []
}

async function handleRead(msg) {
  if (msg.isRead === 1) return
  await readMessage(msg.id)
  ElMessage.success('已标记已读')
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <h1 style="margin-bottom: 20px">消息通知</h1>
    <div style="margin-bottom: 16px">
      <el-switch
        v-model="onlyUnread"
        :active-value="1"
        :inactive-value="0"
        active-text="仅看未读"
        inactive-text="全部"
        @change="load"
      />
    </div>
    <el-table :data="list" border @row-click="handleRead" style="cursor: pointer">
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="typeTag[row.type]">{{ typeMap[row.type] || '消息' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.isRead === 1" type="info">已读</el-tag>
          <el-tag v-else type="danger">未读</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="180" />
    </el-table>
    <el-empty v-if="!list.length" description="暂无消息" />
  </div>
</template>
