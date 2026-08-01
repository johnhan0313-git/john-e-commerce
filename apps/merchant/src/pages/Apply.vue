<template>
  <div>
    <div class="page-header">
      <div>
        <h2>入驻 / 店铺</h2>
        <p class="desc">入驻审核通过后自动开首店；可继续申请更多店铺</p>
      </div>
    </div>

    <div v-if="!loaded" class="panel">
      <div class="panel-body">加载中…</div>
    </div>

    <template v-else-if="me?.merchant?.status === 1">
      <div class="panel mb">
        <div class="panel-pad">
          <h3 class="section-title">我的店铺</h3>
        </div>
        <el-table :data="me.shops || []" empty-text="暂无店铺">
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="name" label="店名" min-width="160" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag size="small" effect="light" :type="shopTagType(row.status)">
                {{ row.statusLabel || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 1"
                link
                type="primary"
                @click="switchShop(row.id)"
              >
                进入
              </el-button>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel form-panel">
        <div class="panel-body">
          <h3 class="section-title">申请新店</h3>
          <el-form label-position="top" class="apply-form">
            <el-form-item label="店铺名称" required>
              <el-input v-model="shopForm.name" placeholder="新店名称" size="large" />
            </el-form-item>
            <el-form-item label="Logo URL">
              <el-input v-model="shopForm.logo" size="large" />
            </el-form-item>
            <el-button type="primary" size="large" :loading="savingShop" @click="applyShop">
              提交开店申请
            </el-button>
          </el-form>
        </div>
      </div>
    </template>

    <div v-else-if="me?.merchant?.status === 0" class="panel result-panel pending">
      <el-result icon="info" title="审核中" sub-title="运营审核通过后将自动创建首店">
        <template #extra>
          <el-button @click="$router.push('/dashboard')">返回概览</el-button>
        </template>
      </el-result>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import client from '@/api/client'
import { useMerchantStore } from '@/stores/merchant'

const router = useRouter()
const merchant = useMerchantStore()
const me = computed(() => merchant.me)
const loaded = computed(() => merchant.loaded)
const saving = ref(false)
const savingShop = ref(false)
const form = reactive({
  name: '',
  logo: '',
  contactName: '',
  contactPhone: '',
  licenseNo: '',
})
const shopForm = reactive({
  name: '',
  logo: '',
})

function shopTagType(status?: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  if (status === 0) return 'warning'
  return 'info'
}

function switchShop(id: number) {
  merchant.setActiveShop(id)
  router.push('/products')
}

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

async function applyShop() {
  if (!shopForm.name.trim()) {
    ElMessage.warning('请填写店铺名称')
    return
  }
  savingShop.value = true
  try {
    await client.post('/shop/apply', {
      name: shopForm.name.trim(),
      logo: shopForm.logo || undefined,
    })
    ElMessage.success('开店申请已提交')
    shopForm.name = ''
    shopForm.logo = ''
    await merchant.fetchMe()
  } finally {
    savingShop.value = false
  }
}

onMounted(() => {
  if (!merchant.loaded) merchant.fetchMe()
})
</script>

<style scoped>
.mb {
  margin-bottom: 16px;
}

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

.muted {
  color: var(--color-muted);
  font-size: 13px;
}
</style>
