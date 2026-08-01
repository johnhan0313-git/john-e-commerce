<template>
  <div>
    <div class="page-header">
      <div>
        <h2>主体 / 店铺</h2>
        <p class="desc">主体入驻审核与开店审核相互独立；通过后可维护主体资料并申请更多店铺</p>
      </div>
    </div>

    <div v-if="!loaded" class="panel">
      <div class="panel-body">加载中…</div>
    </div>

    <template v-else-if="me?.merchant">
      <div class="panel mb">
        <div class="panel-body">
          <div class="section-head">
            <h3 class="section-title">卖家主体</h3>
            <el-tag size="small" effect="light" :type="merchantTagType">
              {{ me.merchant.statusLabel || merchantStatusText }}
            </el-tag>
          </div>
          <p v-if="me.merchant.status === 0" class="hint">主体审核中，可继续修改资料；不影响已有店铺状态</p>
          <p v-else-if="me.merchant.status === 2" class="hint warn">
            主体审核未通过，修改后可重新提交（不会走开店审核）
          </p>
          <p v-else class="hint">联系人、执照等主体资料可随时更新，与开店申请无关</p>

          <el-form label-position="top" class="apply-form">
            <el-form-item label="主体名称" required>
              <el-input v-model="profileForm.name" placeholder="公司 / 个体工商户名称" size="large" />
            </el-form-item>
            <el-form-item label="Logo">
              <ImageUpload v-model="profileForm.logo" folder="logo" :aspect-ratio="1" hint="上传 Logo" />
            </el-form-item>
            <div class="form-row">
              <el-form-item label="联系人" required>
                <el-input v-model="profileForm.contactName" size="large" />
              </el-form-item>
              <el-form-item label="联系电话" required>
                <el-input v-model="profileForm.contactPhone" size="large" />
              </el-form-item>
            </div>
            <el-form-item label="执照号">
              <el-input v-model="profileForm.licenseNo" size="large" />
            </el-form-item>
            <el-button type="primary" size="large" :loading="savingProfile" @click="saveProfile">
              {{ me.merchant.status === 2 ? '重新提交主体审核' : '保存主体资料' }}
            </el-button>
          </el-form>
        </div>
      </div>

      <template v-if="me.merchant.status === 1">
        <div class="panel mb">
          <div class="panel-pad">
            <h3 class="section-title">我的店铺</h3>
            <p class="hint">新店需单独提交开店申请，由运营在「店铺」菜单审核</p>
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
              <el-form-item label="Logo">
                <ImageUpload v-model="shopForm.logo" folder="shop" :aspect-ratio="1" hint="上传店铺 Logo" />
              </el-form-item>
              <el-button type="primary" size="large" :loading="savingShop" @click="applyShop">
                提交开店申请
              </el-button>
            </el-form>
          </div>
        </div>
      </template>
    </template>

    <div v-else class="panel form-panel">
      <div class="panel-body">
        <h3 class="section-title">填写主体入驻资料</h3>
        <p class="hint">提交后进入主体审核；通过后自动开首店，后续新店走开店审核</p>
        <el-form label-position="top" class="apply-form">
          <el-form-item label="主体名称" required>
            <el-input v-model="applyForm.name" placeholder="公司 / 个体工商户名称" size="large" />
          </el-form-item>
          <el-form-item label="Logo">
            <ImageUpload v-model="applyForm.logo" folder="logo" :aspect-ratio="1" hint="上传 Logo" />
          </el-form-item>
          <div class="form-row">
            <el-form-item label="联系人" required>
              <el-input v-model="applyForm.contactName" size="large" />
            </el-form-item>
            <el-form-item label="联系电话" required>
              <el-input v-model="applyForm.contactPhone" size="large" />
            </el-form-item>
          </div>
          <el-form-item label="执照号">
            <el-input v-model="applyForm.licenseNo" size="large" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="savingApply" @click="submitApply">
            提交主体入驻
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import client from '@/api/client'
import ImageUpload from '@/components/ImageUpload.vue'
import { useMerchantStore } from '@/stores/merchant'

const router = useRouter()
const merchant = useMerchantStore()
const me = computed(() => merchant.me)
const loaded = computed(() => merchant.loaded)
const savingApply = ref(false)
const savingProfile = ref(false)
const savingShop = ref(false)

const applyForm = reactive({
  name: '',
  logo: '',
  contactName: '',
  contactPhone: '',
  licenseNo: '',
})
const profileForm = reactive({
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

const merchantStatusText = computed(() => {
  const s = me.value?.merchant?.status
  if (s === 1) return '已通过'
  if (s === 2) return '已拒绝'
  if (s === 0) return '待审核'
  return '未知'
})

const merchantTagType = computed(() => {
  const s = me.value?.merchant?.status
  if (s === 1) return 'success'
  if (s === 2) return 'danger'
  return 'warning'
})

function syncProfileForm() {
  const m = me.value?.merchant
  if (!m) return
  profileForm.name = m.name || ''
  profileForm.logo = m.logo || ''
  profileForm.contactName = m.contactName || ''
  profileForm.contactPhone = m.contactPhone || ''
  profileForm.licenseNo = m.licenseNo || ''
}

watch(me, syncProfileForm, { immediate: true })

function shopTagType(status?: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  if (status === 0) return 'warning'
  return 'info'
}

function switchShop(id: number | string) {
  merchant.setActiveShop(id)
  router.push('/products')
}

async function submitApply() {
  if (!applyForm.name.trim() || !applyForm.contactName.trim() || !applyForm.contactPhone.trim()) {
    ElMessage.warning('请填写必填项')
    return
  }
  savingApply.value = true
  try {
    await client.post('/merchant/apply', {
      name: applyForm.name.trim(),
      logo: applyForm.logo || undefined,
      contactName: applyForm.contactName.trim(),
      contactPhone: applyForm.contactPhone.trim(),
      licenseNo: applyForm.licenseNo || undefined,
    })
    ElMessage.success('已提交主体入驻，等待运营审核')
    await merchant.fetchMe()
  } finally {
    savingApply.value = false
  }
}

async function saveProfile() {
  if (!profileForm.name.trim() || !profileForm.contactName.trim() || !profileForm.contactPhone.trim()) {
    ElMessage.warning('请填写必填项')
    return
  }
  savingProfile.value = true
  try {
    await client.put('/merchant/me', {
      name: profileForm.name.trim(),
      logo: profileForm.logo || undefined,
      contactName: profileForm.contactName.trim(),
      contactPhone: profileForm.contactPhone.trim(),
      licenseNo: profileForm.licenseNo || undefined,
    })
    ElMessage.success(
      me.value?.merchant?.status === 2 ? '已重新提交主体审核' : '主体资料已保存',
    )
    await merchant.fetchMe()
  } finally {
    savingProfile.value = false
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
    ElMessage.success('开店申请已提交，等待店铺审核')
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

.section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.section-head .section-title {
  margin-bottom: 0;
}

.hint {
  margin: 0 0 16px;
  color: var(--color-muted);
  font-size: 13px;
  line-height: 1.5;
}

.hint.warn {
  color: var(--el-color-danger);
}

.muted {
  color: var(--color-muted);
  font-size: 13px;
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
