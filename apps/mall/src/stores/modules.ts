import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'
import type { R } from '@/types'

interface TenantModule {
  moduleCode: string
  moduleName?: string
  status?: number
}

export const useModulesStore = defineStore('modules', () => {
  const list = ref<string[]>([])
  const loaded = ref(false)

  async function fetch() {
    try {
      const res = await client.get('/tenant/modules') as R<TenantModule[]>
      list.value = (res.data || []).map((m) => m.moduleCode)
    } catch {
      list.value = []
    }
    loaded.value = true
  }

  function isEnabled(code: string) {
    return list.value.includes(code)
  }

  return { list, loaded, fetch, isEnabled }
})
