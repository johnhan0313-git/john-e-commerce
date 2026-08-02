<template>
  <div class="rich-text-editor">
    <Toolbar
      :editor="editorRef"
      :default-config="toolbarConfig"
      mode="default"
      class="toolbar"
    />
    <Editor
      v-model="html"
      :default-config="editorConfig"
      mode="default"
      class="editor-body"
      @onCreated="onCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, shallowRef } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'
import { ElMessage } from 'element-plus'
import client from '@/api/client'
import type { R } from '@/types'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    folder?: string
    height?: number
    placeholder?: string
  }>(),
  {
    modelValue: '',
    folder: 'product',
    height: 260,
    placeholder: '请输入商品详情',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const editorRef = shallowRef<IDomEditor>()
const heightPx = computed(() => `${props.height}px`)

const html = computed({
  get: () => props.modelValue || '',
  set: (v: string) => emit('update:modelValue', v === '<p><br></p>' ? '' : v),
})

const toolbarConfig: Partial<IToolbarConfig> = {
  excludeKeys: [
    'group-video',
    'insertVideo',
    'uploadVideo',
    'fullScreen',
    'codeBlock',
    'todo',
  ],
}

const editorConfig: Partial<IEditorConfig> = {
  placeholder: props.placeholder,
  MENU_CONF: {
    uploadImage: {
      async customUpload(file: File, insertFn: (url: string, alt?: string, href?: string) => void) {
        if (!file.type.startsWith('image/')) {
          ElMessage.warning('请选择图片文件')
          return
        }
        if (file.size > 8 * 1024 * 1024) {
          ElMessage.warning('图片不能超过 8MB')
          return
        }
        const form = new FormData()
        form.append('file', file)
        const res = (await client.post(
          `/file/upload?folder=${encodeURIComponent(props.folder)}`,
          form,
          { timeout: 60000 },
        )) as R<{ url: string }>
        if (!res.data?.url) {
          ElMessage.error('上传失败')
          return
        }
        insertFn(res.data.url, file.name, res.data.url)
      },
    },
  },
}

function onCreated(editor: IDomEditor) {
  editorRef.value = editor
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
    editorRef.value = undefined
  }
})
</script>

<style scoped>
.rich-text-editor {
  width: 100%;
  max-width: 720px;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}
.toolbar {
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.editor-body {
  height: v-bind(heightPx);
  overflow-y: hidden;
}
.rich-text-editor :deep(.w-e-text-container) {
  background: #fff;
}
.rich-text-editor :deep(.w-e-bar-item-menus-container),
.rich-text-editor :deep(.w-e-select-list),
.rich-text-editor :deep(.w-e-drop-panel),
.rich-text-editor :deep(.w-e-modal) {
  z-index: 4000 !important;
}
</style>
