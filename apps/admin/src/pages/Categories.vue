<template>
  <div>
    <div class="page-header">
      <div>
        <h2>商品类目</h2>
        <p class="desc">树形类目，供建品归属与商城筛选</p>
      </div>
      <div class="page-header-actions">
        <el-button type="primary" @click="openCreate(null)">新增一级类目</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table
        v-loading="loading"
        :data="tree"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" label="类目名称" min-width="220" />
        <el-table-column prop="id" label="ID" width="180" />
        <el-table-column prop="level" label="层级" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openCreate(row)">加子类</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑类目' : form.parentId ? '新增子类目' : '新增一级类目'"
      width="480px"
      destroy-on-close
    >
      <el-form label-width="90px">
        <el-form-item v-if="parentLabel" label="上级">
          <el-input :model-value="parentLabel" disabled />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.name" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import client from '@/api/client'
import type { Category, R } from '@/types'

const tree = ref<Category[]>([])
const loading = ref(false)
const visible = ref(false)
const saving = ref(false)
const parentLabel = ref('')
const form = reactive({
  id: '' as string | number | '',
  parentId: '' as string | number | '',
  name: '',
  sortOrder: 0,
})

async function load() {
  loading.value = true
  try {
    const res = (await client.get('/category/tree')) as R<Category[]>
    tree.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openCreate(parent: Category | null) {
  form.id = ''
  // 雪花 ID 超过 JS 安全整数，禁止 Number()，保持字符串
  form.parentId = parent ? String(parent.id) : ''
  form.name = ''
  form.sortOrder = 0
  parentLabel.value = parent ? parent.name : ''
  visible.value = true
}

function openEdit(row: Category) {
  form.id = String(row.id)
  form.parentId = row.parentId != null && String(row.parentId) !== '0' ? String(row.parentId) : ''
  form.name = row.name || ''
  form.sortOrder = row.sortOrder ?? 0
  parentLabel.value = ''
  visible.value = true
}

async function save() {
  if (!form.name.trim()) {
    ElMessage.warning('请填写名称')
    return
  }
  saving.value = true
  try {
    const payload = {
      name: form.name.trim(),
      parentId: form.parentId ? form.parentId : 0,
      sortOrder: form.sortOrder ?? 0,
    }
    if (form.id) {
      await client.put(`/category/${form.id}`, payload)
      ElMessage.success('已保存')
    } else {
      await client.post('/category', payload)
      ElMessage.success('已创建')
    }
    visible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row: Category) {
  await ElMessageBox.confirm(`确认删除类目「${row.name}」？`, '提示')
  await client.delete(`/category/${row.id}`)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>
