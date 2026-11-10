<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyOrders, updateOrderStatus, getGoodsDetail, createReview } from '../api'

const orders = ref([])
const statusMap = { 1: '已拍', 2: '面交中', 3: '已完成', 4: '已取消' }
const statusType = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info' }

const reviewDialog = ref(false)
const reviewForm = ref({ orderId: null, score: 5, content: '' })

async function load() {
  const res = await getMyOrders()
  const list = res.data || []
  const withTitle = await Promise.all(
    list.map(async (o) => {
      try {
        const g = await getGoodsDetail(o.goodsId)
        return { ...o, goodsTitle: g.data?.title || `商品#${o.goodsId}` }
      } catch (e) {
        return { ...o, goodsTitle: `商品#${o.goodsId}` }
      }
    })
  )
  orders.value = withTitle
}

async function changeStatus(order, status) {
  await updateOrderStatus(order.id, status)
  ElMessage.success('状态已更新')
  load()
}

function openReview(order) {
  reviewForm.value = { orderId: order.id, score: 5, content: '' }
  reviewDialog.value = true
}

async function submitReview() {
  await createReview(reviewForm.value)
  ElMessage.success('评价成功')
  reviewDialog.value = false
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2 style="margin-bottom: 20px">我的订单</h2>
    <el-table :data="orders" border>
      <el-table-column prop="orderNo" label="订单号" width="190" />
      <el-table-column prop="goodsTitle" label="商品" />
      <el-table-column prop="price" label="价格" width="120">
        <template #default="{ row }">￥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType[row.status]">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" />
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="primary" @click="changeStatus(row, 2)">
            确认面交
          </el-button>
          <el-button v-if="row.status === 2" size="small" type="success" @click="changeStatus(row, 3)">
            完成交易
          </el-button>
          <el-button v-if="row.status === 1" size="small" @click="changeStatus(row, 4)">取消</el-button>
          <el-button v-if="row.status === 3" size="small" type="warning" @click="openReview(row)">评价</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!orders.length" description="暂无订单" />

    <el-dialog v-model="reviewDialog" title="评价订单" width="420px">
      <el-form label-width="60px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.score" />
        </el-form-item>
        <el-form-item label="评价">
          <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="说说交易体验..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>
