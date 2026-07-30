<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2>管理后台登录</h2>
      <el-form label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="手机号">
          <el-input v-model="phone" placeholder="13800000000" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" show-password placeholder="密码" />
        </el-form-item>
        <el-alert v-if="err" :title="err" type="error" show-icon :closable="false" class="mb" />
        <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useModulesStore } from '@/stores/modules'

const phone = ref('13800000000')
const password = ref('admin123')
const err = ref('')
const loading = ref(false)
const auth = useAuthStore()
const modules = useModulesStore()
const router = useRouter()

async function onSubmit() {
  err.value = ''
  loading.value = true
  try {
    await auth.login(phone.value.trim(), password.value)
    await modules.fetch()
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e: any) {
    err.value = e.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(160deg, #f0f4f8, #ffffff 45%, #eef2f7);
}

.login-card {
  width: min(100% - 32px, 400px);
}

.mb {
  margin-bottom: 16px;
}

h2 {
  margin: 0 0 20px;
}
</style>
