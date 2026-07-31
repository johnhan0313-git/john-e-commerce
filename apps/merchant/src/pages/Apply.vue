<template>
  <div>
    <div class="page-header">
      <div>
        <h2>入驻 / 店铺</h2>
        <p class="desc">提交资料 · 等待运营审核 · 自动开店</p>
      </div>
    </div>

    <div v-if="!loaded" class="panel">
      <div class="panel-body">加载中…</div>
    </div>

    <div v-else-if="me?.merchant?.status === 1 && me.shop" class="panel result-panel success">
      <el-result icon="success" title="已开店" :sub-title="`店铺：${me.shop.name}`">
        <template #extra>
          <el-button type="primary" @click="$router.push('/products')">管理商品</el-button>
          <el-button @click="$router.push('/dashboard')">返回概览</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="me?.merchant?.status === 0" class="panel result-panel pending">
      <el-result icon="info" title="审核中" sub-title="运营审核通过后将自动创建店铺">
        <template #extra>
          <el-button @click="$router.push('/dashboard')">返回概览</el-button>
        </template>
      </el-result>
      <div class="steps">
        <div class="step done">提交资料</div>
        <div class="step active">运营审核</div>
        <div class="step">开店就绪</div>
      </div>
    </div>

    <div v-else-if="me?.merchant?.status === 2" class="panel result-panel fail">
      <el-result
        icon="error"
        title="审核未通过"
        sub-title="可联系运营后重新准备资料（需换账号或联系运营重置）"
      />
    </div>

    <div v-else class="panel form-panel">
      <div class="panel-body">
        <h3 class="section-title">填写入驻资料</h3>
        <el-form label-position="top" class="apply-form">
          <el-form-item label="卖家/店名" required>
            <el-input v-model="form.name" placeholder="入驻后将作为默认店铺名" size="large" />
          </el-form-item>
          <el-form-item label="Logo URL">
            <el-input v-model="form.logo" size="large" />
          </el-form-item>
          <div class="form-row">
            <el-form-item label="联系人" required>
              <el-input v-model="form.contactName" size="large" />
            </el-form-item>
            <el-form-item label="联系电话" required>
              <el-input v-model="form.contactPhone" size="large" />
            </el-form-item>
          </div>
          <el-form-item label="执照号">
            <el-input v-model="form.licenseNo" size="large" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="saving" @click="submit">提交入驻</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'
import { useMerchantStore } from '@/stores/merchant'

const merchant = useMerchantStore()
const me = computed(() => merchant.me)
const loaded = computed(() => merchant.loaded)
const saving = ref(false)
const form = reactive({
  name: '',
  logo: '',
  contactName: '',
  contactPhone: '',
  licenseNo: '',
})

async function submit() {
  if (!form.name.trim() || !form.contactName.trim() || !form.contactPhone.trim()) {
    ElMessage.warning('请填写必填项')
    return
  }
  saving.value = true
  try {
    await client.post('/merchant/apply', {
      name: form.name.trim(),
      logo: form.logo || undefined,
      contactName: form.contactName.trim(),
      contactPhone: form.contactPhone.trim(),
      licenseNo: form.licenseNo || undefined,
    })
    ElMessage.success('已提交，等待运营审核')
    await merchant.fetchMe()
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (!merchant.loaded) merchant.fetchMe()
})
</script>

<style scoped>
.form-panel {
  max-width: 560px;
}

.apply-form {
  max-width: 480px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.result-panel.success {
  background:
    linear-gradient(180deg, rgba(16, 185, 129, 0.06), transparent 40%),
    var(--color-surface);
}

.result-panel.pending {
  background:
    linear-gradient(180deg, rgba(14, 165, 233, 0.06), transparent 40%),
    var(--color-surface);
}

.result-panel.fail {
  background:
    linear-gradient(180deg, rgba(239, 68, 68, 0.06), transparent 40%),
    var(--color-surface);
}

.steps {
  display: flex;
  gap: 8px;
  padding: 0 32px 28px;
  justify-content: center;
  flex-wrap: wrap;
}

.step {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-muted);
  background: #f1f5f9;
  border: 1px solid var(--color-border);
  border-radius: 99px;
  padding: 6px 14px;
}

.step.done {
  color: #0f766e;
  background: #ccfbf1;
  border-color: #99f6e4;
}

.step.active {
  color: #0369a1;
  background: #e0f2fe;
  border-color: #bae6fd;
}
</style>
