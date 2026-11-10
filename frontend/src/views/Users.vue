<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserList, getMyFollows, follow } from '../api'

const router = useRouter()
const users = ref([])
const followedIds = ref([])

async function load() {
  const [u, f] = await Promise.all([getUserList(), getMyFollows()])
  users.value = u.data || []
  followedIds.value = f.data || []
}

async function toggleFollow(u) {
  const isFollowed = followedIds.value.includes(u.id)
  await follow(u.id, !isFollowed)
  ElMessage.success(isFollowed ? '已取消关注' : '已关注')
  if (isFollowed) {
    followedIds.value = followedIds.value.filter((id) => id !== u.id)
  } else {
    followedIds.value.push(u.id)
  }
}

function goHome(id) {
  router.push(`/user/${id}`)
}

onMounted(load)
</script>

<template>
  <div>
    <h1 style="margin-bottom: 20px">校园用户</h1>
    <el-row :gutter="16">
      <el-col v-for="u in users" :key="u.id" :span="6" style="margin-bottom: 16px">
        <el-card class="user-card" @click="goHome(u.id)">
          <div class="avatar">{{ (u.nickname || '?').charAt(0) }}</div>
          <h3 class="name">{{ u.nickname }}</h3>
          <p class="school">{{ u.school || '未知学院' }}</p>
          <div class="meta">
            <el-tag size="small" type="warning">信誉 {{ u.credit || 0 }}</el-tag>
            <el-tag size="small" type="info">{{ u.goodsCount }} 件在售</el-tag>
          </div>
          <el-button
            size="small"
            :type="followedIds.includes(u.id) ? 'default' : 'primary'"
            style="width: 100%; margin-top: 10px"
            @click.stop="toggleFollow(u)"
          >
            {{ followedIds.includes(u.id) ? '已关注' : '关注' }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!users.length" description="暂无用户" />
  </div>
</template>

<style scoped>
.user-card {
  text-align: center;
  cursor: pointer;
}
.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--accent);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin: 0 auto 10px;
}
.name {
  font-size: 16px;
  margin-bottom: 4px;
}
.school {
  color: #909399;
  font-size: 13px;
  margin-bottom: 10px;
}
.meta {
  display: flex;
  justify-content: center;
  gap: 8px;
}
</style>
