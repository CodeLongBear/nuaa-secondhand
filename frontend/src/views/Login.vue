<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { sendCode, login } from '../api'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({ studentNo: '', code: '' })
const countdown = ref(0)

async function handleSendCode() {
  if (!form.value.studentNo) {
    ElMessage.warning('请输入学号')
    return
  }
  const res = await sendCode(form.value.studentNo)
  ElMessage.success('验证码已发送：' + res.data + '（开发模式明文展示）')
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

async function handleLogin() {
  if (!form.value.studentNo || !form.value.code) {
    ElMessage.warning('请填写学号和验证码')
    return
  }
  const res = await login(form.value)
  userStore.setToken(res.data)
  ElMessage.success('登录成功')
  router.push('/home')
}
</script>

<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2 class="title">校园二手交易平台</h2>
      <p class="subtitle">学号 + 验证码登录（自动注册）</p>
      <el-form @submit.prevent>
        <el-form-item>
          <el-input v-model="form.studentNo" placeholder="请输入学号" size="large" />
        </el-form-item>
        <el-form-item>
          <div class="code-row">
            <el-input v-model="form.code" placeholder="验证码" size="large" />
            <el-button size="large" :disabled="countdown > 0" @click="handleSendCode">
              {{ countdown > 0 ? countdown + 's' : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
}
.login-card {
  width: 420px;
  padding: 20px;
}
.title {
  text-align: center;
  color: var(--text);
  margin-bottom: 8px;
}
.subtitle {
  text-align: center;
  color: #909399;
  margin-bottom: 24px;
  font-size: 14px;
}
.code-row {
  display: flex;
  gap: 12px;
  width: 100%;
}
.login-btn {
  width: 100%;
}
</style>
