import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'
import type { R, TenantModule } from '@/types'

export const useModulesStore = defineStore('modules', () => {
  const list = ref<string[]>([])
  const loaded = ref(false)

  async function fetch() {
    try {
      const res = await client.get('/tenant/modules') as R<TenantModule[]>
      list.value = (res.data || [])
        .filter((m) => m.status !== 0)
        .map((m) => m.moduleCode)
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
