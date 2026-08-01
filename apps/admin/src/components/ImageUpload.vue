<template>
  <div class="image-upload">
    <div v-if="modelValue" class="preview">
      <img :src="modelValue" alt="preview" />
      <div class="preview-actions">
        <el-button size="small" @click="pick">更换</el-button>
        <el-button size="small" type="danger" plain @click="clear">移除</el-button>
      </div>
    </div>
    <div
      v-else
      class="dropzone"
      :class="{ disabled: uploading }"
      @click="pick"
    >
      <el-icon :size="22"><Plus /></el-icon>
      <span>{{ hint || '上传并裁剪图片' }}</span>
    </div>
    <input
      ref="inputRef"
      type="file"
      accept="image/jpeg,image/png,image/webp,image/gif"
      class="hidden-input"
      @change="onFileChange"
    />

    <el-dialog
      v-model="cropVisible"
      title="裁剪图片"
      width="560px"
      append-to-body
      destroy-on-close
      @closed="destroyCropper"
    >
      <div class="crop-wrap">
        <img ref="cropImgRef" :src="cropSrc" alt="crop" />
      </div>
      <template #footer>
        <el-button @click="cropVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="confirmCrop">
          确认并上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'
import client from '@/api/client'
import type { R } from '@/types'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    folder?: string
    aspectRatio?: number
    hint?: string
  }>(),
  {
    modelValue: '',
    folder: 'misc',
    aspectRatio: 1,
    hint: '',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const inputRef = ref<HTMLInputElement | null>(null)
const cropImgRef = ref<HTMLImageElement | null>(null)
const cropVisible = ref(false)
const cropSrc = ref('')
const uploading = ref(false)
let cropper: Cropper | null = null
let objectUrl: string | null = null

function pick() {
  if (uploading.value) return
  inputRef.value?.click()
}

function clear() {
  emit('update:modelValue', '')
}

function revokeObjectUrl() {
  if (objectUrl) {
    URL.revokeObjectURL(objectUrl)
    objectUrl = null
  }
}

function destroyCropper() {
  cropper?.destroy()
  cropper = null
  cropSrc.value = ''
  revokeObjectUrl()
}

async function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 8 * 1024 * 1024) {
    ElMessage.warning('图片不能超过 8MB')
    return
  }
  revokeObjectUrl()
  objectUrl = URL.createObjectURL(file)
  cropSrc.value = objectUrl
  cropVisible.value = true
  await nextTick()
  if (!cropImgRef.value) return
  cropper?.destroy()
  cropper = new Cropper(cropImgRef.value, {
    aspectRatio: props.aspectRatio > 0 ? props.aspectRatio : NaN,
    viewMode: 1,
    autoCropArea: 1,
    background: false,
    movable: true,
    zoomable: true,
    scalable: false,
    rotatable: false,
  })
}

function canvasToBlob(canvas: HTMLCanvasElement): Promise<Blob> {
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => (blob ? resolve(blob) : reject(new Error('裁剪失败'))),
      'image/jpeg',
      0.92,
    )
  })
}

async function confirmCrop() {
  if (!cropper) return
  uploading.value = true
  try {
    const canvas = cropper.getCroppedCanvas({
      maxWidth: 1600,
      maxHeight: 1600,
      imageSmoothingEnabled: true,
      imageSmoothingQuality: 'high',
    })
    if (!canvas) {
      ElMessage.error('裁剪失败')
      return
    }
    const blob = await canvasToBlob(canvas)
    const form = new FormData()
    form.append('file', blob, `upload-${Date.now()}.jpg`)
    const res = await client.post(`/file/upload?folder=${encodeURIComponent(props.folder)}`, form, {
      timeout: 60000,
    }) as R<{ url: string }>
    if (!res.data?.url) {
      ElMessage.error('上传失败')
      return
    }
    emit('update:modelValue', res.data.url)
    cropVisible.value = false
    ElMessage.success('上传成功')
  } catch {
    /* axios interceptor shows message */
  } finally {
    uploading.value = false
  }
}

onBeforeUnmount(() => {
  destroyCropper()
})
</script>

<style scoped>
.image-upload {
  width: 100%;
}

.dropzone {
  width: 140px;
  height: 140px;
  border: 1px dashed var(--color-border, #dcdfe6);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--color-muted, #909399);
  cursor: pointer;
  background: #fafafa;
  transition: border-color 0.15s, color 0.15s;
  user-select: none;
}

.dropzone:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

.dropzone.disabled {
  opacity: 0.6;
  pointer-events: none;
}

.dropzone span {
  font-size: 12px;
  padding: 0 8px;
  text-align: center;
  line-height: 1.3;
}

.preview {
  width: 140px;
}

.preview img {
  width: 140px;
  height: 140px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid var(--color-border, #e5e7eb);
  display: block;
  background: #fff;
}

.preview-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.hidden-input {
  display: none;
}

.crop-wrap {
  max-height: 420px;
  background: #111827;
}

.crop-wrap img {
  display: block;
  max-width: 100%;
}
</style>
