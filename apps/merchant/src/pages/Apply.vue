<template>
  <div>
    <h2>入驻 / 店铺</h2>

    <template v-if="!loaded">加载中…</template>

    <el-result v-else-if="me?.merchant?.status === 1 && me.shop" icon="success" title="已开店"
      :sub-title="`店铺：${me.shop.name}`" />

    <el-result v-else-if="me?.merchant?.status === 0" icon="info" title="审核中"
      sub-title="运营审核通过后将自动创建店铺" />

    <el-result v-else-if="me?.merchant?.status === 2" icon="error" title="审核未通过"
      sub-title="可联系运营后重新准备资料（需换账号或联系运营重置）" />

    <el-card v-else style="max-width: 520px; margin-top: 16px">
      <el-form label-width="100px">
        <el-form-item label="卖家/店名" required>
          <el-input v-model="form.name" placeholder="入驻后将作为默认店铺名" />
        </el-form-item>
        <el-form-item label="Logo URL">
          <el-input v-model="form.logo" />
        </el-form-item>
        <el-form-item label="联系人" required>
          <el-input v-model="form.contactName" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="执照号">
          <el-input v-model="form.licenseNo" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submit">提交入驻</el-button>
        </el-form-item>
      </el-form>
    </el-card>
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
h2 { margin: 0; }
</style>
