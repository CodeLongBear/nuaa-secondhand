<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyOrders, updateOrderStatus } from '../api'

const orders = ref([])
const statusMap = { 1: '已拍', 2: '面交中', 3: '已完成', 4: '已取消' }
const statusType = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info' }

async function load() {
  const res = await getMyOrders()
  orders.value = res.data
}

async function changeStatus(order, status) {
  await updateOrderStatus(order.id, status)
  ElMessage.success('状态已更新')
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2 style="margin-bottom: 20px">我的订单</h2>
    <el-table :data="orders" border>
      <el-table-column prop="orderNo" label="订单号" width="190" />
      <el-table-column prop="goodsId" label="商品ID" width="100" />
      <el-table-column prop="price" label="价格" width="120">
        <template #default="{ row }">￥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType[row.status]">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="primary" @click="changeStatus(row, 2)">
            确认面交
          </el-button>
          <el-button v-if="row.status === 2" size="small" type="success" @click="changeStatus(row, 3)">
            完成交易
          </el-button>
          <el-button v-if="row.status === 1 || row.status === 2" size="small" @click="changeStatus(row, 4)">
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!orders.length" description="暂无订单" />
  </div>
</template>
