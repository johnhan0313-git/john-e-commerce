<template>
  <div>
    <div class="page-header">
      <div>
        <h2>商城品牌</h2>
        <p class="desc">配置展示名称、Logo 与 favicon；mall / admin / merchant 三端标签页与顶栏同步</p>
      </div>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </div>

    <div v-loading="loading" class="panel">
      <div class="panel-body form-wrap">
        <el-form label-position="top" style="max-width: 480px">
          <el-form-item label="租户名称">
            <el-input :model-value="meta.name || '—'" disabled />
          </el-form-item>
          <el-form-item label="展示名称">
            <el-input
              v-model="form.displayName"
              maxlength="100"
              show-word-limit
              placeholder="留空则 mall 使用租户名称"
            />
          </el-form-item>
          <el-form-item label="商城 Logo">
            <ImageUpload v-model="form.logo" folder="branding" :aspect-ratio="1" hint="上传正方形 Logo" />
            <p class="hint">建议 1:1，mall 顶栏按 28–32px 高度展示</p>
          </el-form-item>
          <el-form-item label="Favicon">
            <ImageUpload v-model="form.favicon" folder="branding" :aspect-ratio="1" hint="上传 favicon" />
            <p class="hint">浏览器标签图标，可选</p>
          </el-form-item>
        </el-form>

        <div class="preview">
          <div class="preview-label">mall 顶栏预览</div>
          <div class="preview-bar">
            <img v-if="form.logo" :src="form.logo" alt="logo" class="preview-logo" />
            <span v-else class="preview-mark">{{ markLetter }}</span>
            <strong>{{ previewName }}</strong>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import client from '@/api/client'
import { useBrandingStore } from '@/stores/branding'
import type { R, TenantBranding } from '@/types'

const branding = useBrandingStore()
const loading = ref(false)
const saving = ref(false)
const meta = reactive({ name: '', slug: '' })
const form = reactive({
  displayName: '',
  logo: '',
  favicon: '',
})

const previewName = computed(
  () => form.displayName.trim() || meta.name || 'John Mall',
)
const markLetter = computed(() => (previewName.value.trim().charAt(0) || 'J').toUpperCase())

async function load() {
  loading.value = true
  try {
    const res = (await client.get('/tenant/branding')) as R<TenantBranding>
    const d = res.data
    meta.name = d?.name || ''
    meta.slug = d?.slug || ''
    form.displayName = d?.displayName || ''
    form.logo = d?.logo || ''
    form.favicon = d?.favicon || ''
    branding.setFrom(d)
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    const res = (await client.put('/tenant/branding', {
      displayName: form.displayName.trim() || null,
      logo: form.logo || null,
      favicon: form.favicon || null,
    })) as R<TenantBranding>
    const d = res.data
    form.displayName = d?.displayName || ''
    form.logo = d?.logo || ''
    form.favicon = d?.favicon || ''
    branding.setFrom(d)
    ElMessage.success('品牌已保存，三端标签页将同步更新')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.form-wrap {
  display: grid;
  grid-template-columns: minmax(0, 480px) minmax(220px, 1fr);
  gap: 32px;
  align-items: start;
}

.hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--color-muted);
  line-height: 1.4;
}

.preview {
  padding: 16px;
  border: 1px dashed var(--color-border);
  border-radius: 12px;
  background: #f8fafc;
}

.preview-label {
  font-size: 12px;
  color: var(--color-muted);
  margin-bottom: 12px;
  font-weight: 500;
}

.preview-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 48px;
  padding: 0 14px;
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 10px;
}

.preview-logo {
  width: 28px;
  height: 28px;
  object-fit: contain;
  border-radius: 6px;
}

.preview-mark {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #0ea5e9, #0369a1);
}

.preview-bar strong {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-ink);
}

@media (max-width: 900px) {
  .form-wrap {
    grid-template-columns: 1fr;
  }
}
</style>
