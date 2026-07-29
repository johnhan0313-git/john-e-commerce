import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'

export const useModulesStore = defineStore('modules', () => {
  const list = ref<string[]>([])
  const loaded = ref(false)

  async function fetch() {
    try {
      const res: any = await client.get('/tenant/modules')
      list.value = (res.data || []).map((m: any) => m.moduleCode)
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
