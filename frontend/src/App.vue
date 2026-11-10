<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from './store/user'
import { useRouter, useRoute } from 'vue-router'
import { getUnreadCount } from './api'
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const nav = [
  { path: '/home', label: '首页' },
  { path: '/seckill', label: '限时抢购' },
  { path: '/nearby', label: '附近好物' },
  { path: '/feed', label: '关注动态' },
  { path: '/sign', label: '每日签到' },
  { path: '/agent', label: 'AI 助手' },
  { path: '/orders', label: '我的订单' },
  { path: '/favorites', label: '我的收藏' },
  { path: '/messages', label: '消息' },
  { path: '/publish', label: '发布商品' }
]

const unreadCount = ref(0)
let timer = null

async function refreshUnread() {
  try {
    if (userStore.token) {
      const res = await getUnreadCount()
      unreadCount.value = res.data || 0
    } else {
      unreadCount.value = 0
    }
  } catch (e) {
    unreadCount.value = 0
  }
}

onMounted(() => {
  refreshUnread()
  timer = setInterval(refreshUnread, 15000)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="shell">
    <!-- 侧栏（tinted L 框架的左臂） -->
    <aside class="sidebar">
      <div class="brand" @click="router.push('/home')">
        <svg class="mark" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M20 12a8 8 0 1 1-2.34-5.66" />
          <polyline points="20 3 20 9 14 9" />
        </svg>
        <span class="brand-name">校园二手交易</span>
      </div>

      <nav class="nav">
        <a
          v-for="item in nav"
          :key="item.path"
          class="nav-item"
          :class="{ active: route.path === item.path }"
          @click="router.push(item.path)"
        >
          {{ item.label }}
          <span v-if="item.path === '/messages' && unreadCount > 0" class="badge">
            {{ unreadCount > 99 ? '99+' : unreadCount }}
          </span>
        </a>
      </nav>
    </aside>

    <!-- 右半：顶栏 + 白色 sheet -->
    <div class="frame">
      <header class="topbar">
        <span class="topbar-context">南京航空航天大学 · 软件工程课程设计</span>
        <div class="topbar-actions">
          <template v-if="userStore.token">
            <span class="account">{{ userStore.userInfo?.nickname || '已登录' }}</span>
            <el-button link @click="logout">退出</el-button>
          </template>
          <el-button v-else type="primary" size="small" @click="router.push('/login')">登录</el-button>
        </div>
      </header>

      <main class="sheet">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.shell {
  display: flex;
  min-height: 100vh;
  background: var(--ground);
}

/* 侧栏 */
.sidebar {
  width: var(--sidebar-width);
  flex: none;
  padding: 20px 14px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
  cursor: pointer;
}
.mark {
  color: var(--accent);
  flex: none;
}
.brand-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  letter-spacing: 0;
}
.nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nav-item {
  display: block;
  padding: 9px 12px;
  font-size: 14px;
  color: var(--muted);
  border-radius: var(--radius-control);
  cursor: pointer;
  text-decoration: none;
  transition: background-color 0.12s, color 0.12s;
}
.nav-item:hover {
  color: var(--text);
  background: var(--active);
}
.nav-item.active {
  color: var(--text);
  font-weight: 600;
  background: var(--active);
}
.badge {
  display: inline-block;
  margin-left: 8px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #e5484d;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
}

/* 右半框架 */
.frame {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* 顶栏（与侧栏同 tinted ground，无分隔线） */
.topbar {
  height: var(--topbar-height);
  flex: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.topbar-context {
  font-size: var(--fs-small);
  color: var(--muted);
}
.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.account {
  font-size: var(--fs-body);
  color: var(--text);
}

/* 白色 sheet：inset 8px，16px 圆角，1px 边，无阴影 */
.sheet {
  flex: 1;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-sheet);
  margin: 0 var(--sheet-inset) var(--sheet-inset) 0;
  padding: var(--pad);
  overflow: auto;
}
</style>
