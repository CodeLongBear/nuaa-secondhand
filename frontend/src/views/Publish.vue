<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { publishGoods } from '../api'

const router = useRouter()
const form = reactive({
  title: '',
  description: '',
  category: '数码',
  price: null,
  longitude: 118.79,
  latitude: 31.9388,
  location: '将军路校区',
  conditionDesc: '九成新',
  stock: 1,
  isHot: 0
})
const categories = ['数码', '书籍', '生活用品', '服饰', '运动', '其他']

async function submit() {
  if (!form.title || form.price == null) {
    ElMessage.warning('请填写标题和价格')
    return
  }
  await publishGoods(form)
  ElMessage.success('发布成功')
  router.push('/home')
}
</script>

<template>
  <el-card style="max-width: 640px; margin: 0 auto">
    <h2 style="margin-bottom: 20px">发布二手商品</h2>
    <el-form label-width="80px">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="商品标题" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="商品描述" />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="form.category">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
      </el-form-item>
      <el-form-item label="价格">
        <el-input-number v-model="form.price" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="成色">
        <el-select v-model="form.conditionDesc">
          <el-option label="全新" value="全新" />
          <el-option label="九成新" value="九成新" />
          <el-option label="八成新" value="八成新" />
          <el-option label="七成新及以下" value="七成新及以下" />
        </el-select>
      </el-form-item>
      <el-form-item label="位置">
        <el-input v-model="form.location" placeholder="如：将军路校区" />
      </el-form-item>
      <el-form-item label="经纬度">
        <div style="display: flex; gap: 12px; width: 100%">
          <el-input-number v-model="form.longitude" :precision="4" :step="0.01" style="width: 100%" />
          <el-input-number v-model="form.latitude" :precision="4" :step="0.01" style="width: 100%" />
        </div>
      </el-form-item>
      <el-form-item label="库存">
        <el-input-number v-model="form.stock" :min="1" />
      </el-form-item>
      <el-form-item label="热门抢购">
        <el-switch v-model="form.isHot" :active-value="1" :inactive-value="0" active-text="是" inactive-text="否" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit">发布</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>
