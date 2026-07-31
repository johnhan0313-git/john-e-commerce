<template>
  <div class="login-page">
    <aside class="brand-panel">
      <div class="brand-panel-inner">
        <div class="logo-row">
          <span class="mark">M</span>
          <span class="name">John Merchant</span>
        </div>
        <h1>开店经营<br />从这里开始</h1>
        <p>邮箱验证码即登即用，提交入驻资料后即可管理本店商品与订单</p>
        <ul class="features">
          <li>一键入驻审核</li>
          <li>本店商品与 SKU</li>
          <li>订单履约发货</li>
        </ul>
      </div>
      <div class="grid-deco" aria-hidden="true" />
    </aside>

    <main class="form-panel">
      <div class="form-box">
        <h2>卖家登录 / 注册</h2>
        <p class="sub">邮箱验证码登录，未注册将自动开通账号；入驻审核请登录后提交资料</p>
        <el-form label-position="top" @submit.prevent="onSubmit">
          <el-form-item label="邮箱" required>
            <el-input
              v-model="email"
              type="email"
              placeholder="seller@example.com"
              autocomplete="username"
              size="large"
            />
          </el-form-item>
          <el-form-item label="验证码" required>
            <div class="code-row">
              <el-input
                v-model="code"
                placeholder="6 位验证码"
                maxlength="6"
                autocomplete="one-time-code"
                size="large"
              />
              <el-button size="large" :disabled="cooldown > 0 || sending || !email.trim()" @click="sendCode">
                {{ cooldown > 0 ? `${cooldown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-alert v-if="err" :title="err" type="error" show-icon :closable="false" class="mb" />
          <el-button type="primary" native-type="submit" :loading="loading" size="large" class="submit">
            登录 / 注册
          </el-button>
          <p class="hint">本地开发可用固定码 123456（app.auth.fixed-code）</p>
        </el-form>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useMerchantStore } from '@/stores/merchant'

const email = ref('')
const code = ref('')
const err = ref('')
const loading = ref(false)
const sending = ref(false)
const cooldown = ref(0)
let timer: number | undefined

const auth = useAuthStore()
const merchant = useMerchantStore()
const router = useRouter()

async function sendCode() {
  err.value = ''
  if (!email.value.trim()) {
    err.value = '请先填写邮箱'
    return
  }
  sending.value = true
  try {
    await auth.sendCode(email.value.trim())
    ElMessage.success('验证码已发送')
    cooldown.value = 60
    timer = window.setInterval(() => {
      cooldown.value -= 1
      if (cooldown.value <= 0 && timer) {
        clearInterval(timer)
        timer = undefined
      }
    }, 1000)
  } catch (e: any) {
    err.value = e.response?.data?.message || '发送失败'
  } finally {
    sending.value = false
  }
}

async function onSubmit() {
  err.value = ''
  loading.value = true
  try {
    await auth.login(email.value.trim(), code.value.trim())
    merchant.clear()
    await merchant.fetchMe()
    ElMessage.success('登录成功')
    router.push(merchant.isApproved() ? '/dashboard' : '/apply')
  } catch (e: any) {
    err.value = e.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(280px, 1.05fr) minmax(320px, 1fr);
  background: var(--color-bg);
}

.brand-panel {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(20, 184, 166, 0.35), transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(45, 212, 191, 0.18), transparent 45%),
    linear-gradient(160deg, #0f172a 0%, #1e293b 55%, #115e59 100%);
  color: #e2e8f0;
  padding: 48px;
  display: flex;
  align-items: center;
}

.brand-panel-inner {
  position: relative;
  z-index: 1;
  max-width: 420px;
}

.logo-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 36px;
}

.mark {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-weight: 700;
  background: linear-gradient(135deg, #14b8a6, #0f766e);
  color: #fff;
  box-shadow: 0 8px 24px rgba(20, 184, 166, 0.4);
}

.name {
  font-weight: 700;
  font-size: 16px;
  letter-spacing: -0.02em;
}

.brand-panel h1 {
  margin: 0 0 16px;
  font-size: clamp(28px, 4vw, 40px);
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -0.03em;
  color: #f8fafc;
}

.brand-panel p {
  margin: 0 0 28px;
  font-size: 15px;
  line-height: 1.6;
  color: #94a3b8;
}

.features {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.features li {
  font-size: 13px;
  color: #cbd5e1;
  padding-left: 18px;
  position: relative;
}

.features li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 7px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2dd4bf;
  box-shadow: 0 0 0 3px rgba(45, 212, 191, 0.2);
}

.grid-deco {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.06) 1px, transparent 1px);
  background-size: 40px 40px;
  mask-image: radial-gradient(ellipse at center, black 20%, transparent 75%);
  pointer-events: none;
}

.form-panel {
  display: grid;
  place-items: center;
  padding: 40px 24px;
}

.form-box {
  width: min(100%, 400px);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 36px 32px;
  box-shadow: var(--shadow);
}

.form-box h2 {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.sub {
  margin: 0 0 24px;
  font-size: 13px;
  color: var(--color-muted);
  line-height: 1.5;
}

.code-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.mb {
  margin-bottom: 16px;
}

.submit {
  width: 100%;
  margin-top: 4px;
}

.hint {
  margin: 14px 0 0;
  font-size: 12px;
  color: var(--color-muted);
  line-height: 1.5;
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    padding: 32px 24px;
    min-height: 220px;
  }

  .brand-panel h1 {
    font-size: 26px;
  }

  .features {
    display: none;
  }
}
</style>
