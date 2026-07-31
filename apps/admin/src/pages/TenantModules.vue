<template>
  <div>
    <div class="page-header">
      <div>
        <h2>模块配置</h2>
        <p class="desc">开通 PRODUCT / TRADE / PAYMENT 等模块后，对应菜单与接口才可用</p>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="rows" class="module-table">
        <el-table-column prop="moduleCode" label="编码" width="140">
          <template #default="{ row }">
            <code class="code">{{ row.moduleCode }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="moduleName" label="名称" width="160" />
        <el-table-column prop="description" label="说明" min-width="200" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small" effect="light">
              {{ row.enabled ? '已开通' : '未开通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled"
              :loading="row.switching"
              @change="(val: string | number | boolean) => toggle(row, Boolean(val))"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'
import { useModulesStore } from '@/stores/modules'
import type { ModuleDef, R, TenantModule } from '@/types'

interface Row extends ModuleDef {
  enabled: boolean
  switching?: boolean
}

const rows = ref<Row[]>([])
const loading = ref(false)
const modules = useModulesStore()

async function load() {
  loading.value = true
  try {
    const [defsRes, enabledRes] = await Promise.all([
      client.get('/module-def') as Promise<R<ModuleDef[]>>,
      client.get('/tenant/modules') as Promise<R<TenantModule[]>>,
    ])
    const enabledSet = new Set(
      (enabledRes.data || [])
        .filter((m) => m.status !== 0)
        .map((m) => m.moduleCode)
    )
    rows.value = (defsRes.data || []).map((d) => ({
      ...d,
      enabled: enabledSet.has(d.moduleCode),
      switching: false,
    }))
  } finally {
    loading.value = false
  }
}

async function toggle(row: Row, enabled: boolean) {
  row.switching = true
  try {
    if (enabled) {
      await client.post('/tenant/modules', { moduleCode: row.moduleCode })
      ElMessage.success(`已开通 ${row.moduleCode}`)
    } else {
      await client.delete(`/tenant/modules/${row.moduleCode}`)
      ElMessage.success(`已关闭 ${row.moduleCode}`)
    }
    row.enabled = enabled
    await modules.fetch()
  } catch {
    /* interceptor shows message */
  } finally {
    row.switching = false
  }
}

onMounted(load)
</script>

<style scoped>
.code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  background: #f1f5f9;
  color: #0369a1;
  padding: 2px 8px;
  border-radius: 6px;
}

.module-table :deep(.el-table__row:hover > td.el-table__cell) {
  background: #f0f9ff !important;
}
</style>
