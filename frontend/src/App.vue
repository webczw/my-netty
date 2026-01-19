<template>
  <div class="container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>Netty 消息发送</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        label-position="right"
      >
        <el-form-item label="客户端消息" prop="clientMsg">
          <el-input
            v-model="formData.clientMsg"
            placeholder="请输入消息内容"
            clearable
          />
        </el-form-item>

        <el-form-item label="消息开关" prop="msgSwitch">
          <el-switch
            v-model="formData.msgSwitch"
            active-value="1"
            inactive-value="0"
            active-text="开启"
            inactive-text="关闭"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            :icon="Promotion"
            @click="handleSubmit"
          >
            提交消息
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="responseData" class="result-card">
      <template #header>
        <div class="card-header">
          <span>服务端响应</span>
          <el-tag :type="responseData.success ? 'success' : 'danger'">
            {{ responseData.success ? '成功' : '失败' }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="响应消息">
          {{ responseData.message }}
        </el-descriptions-item>

        <el-descriptions-item v-if="responseData.data" label="UUID">
          {{ responseData.data.uuid }}
        </el-descriptions-item>

        <el-descriptions-item v-if="responseData.data" label="客户端消息">
          {{ responseData.data.clientMsg }}
        </el-descriptions-item>

        <el-descriptions-item v-if="responseData.data" label="服务端消息">
          {{ responseData.data.serverMsg }}
        </el-descriptions-item>

        <el-descriptions-item v-if="responseData.data" label="消息开关">
          <el-tag :type="responseData.data.msgSwitch === '1' ? 'success' : 'info'">
            {{ responseData.data.msgSwitch === '1' ? '开启' : '关闭' }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item v-if="responseData.data" label="服务器时间">
          {{ formatDate(responseData.data.serverTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import axios from 'axios'

const formRef = ref(null)
const loading = ref(false)
const responseData = ref(null)

const formData = reactive({
  clientMsg: '',
  msgSwitch: '0'
})

const formRules = {
  clientMsg: [
    { required: true, message: '请输入客户端消息内容', trigger: 'blur' },
    { min: 1, max: 500, message: '长度在 1 到 500 个字符', trigger: 'blur' }
  ]
}

const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const handleSubmit = async () => {
  try {
    // 表单校验，校验失败时直接返回，不触发后端调用
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) {
      return
    }

    loading.value = true
    responseData.value = null

    const response = await axios.post('/netty/helloNetty', formData, {
      headers: {
        'Content-Type': 'application/json'
      }
    })

    responseData.value = response.data

    if (response.data.success) {
      ElMessage.success('消息提交成功！')
    } else {
      ElMessage.error(response.data.message || '消息提交失败')
    }
  } catch (error) {
    console.error('请求错误:', error)
    if (error.response) {
      ElMessage.error(error.response.data.message || '服务端错误')
    } else if (error.request) {
      ElMessage.error('网络错误，请检查后端服务是否启动')
    } else {
      ElMessage.error('提交失败：' + (error.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formRef.value.resetFields()
  responseData.value = null
}
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 18px;
}

.form-card,
.result-card {
  margin-bottom: 20px;
}
</style>
