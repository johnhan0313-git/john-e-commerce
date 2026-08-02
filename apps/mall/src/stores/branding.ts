import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import client from '@/api/client'
import type { R, TenantBranding } from '@/types'

const DEFAULT_NAME = 'John Mall'

function applyDocumentMeta(name: string, favicon?: string | null) {
  document.title = name
  if (!favicon) return
  let link = document.querySelector<HTMLLinkElement>("link[rel='icon']")
  if (!link) {
    link = document.createElement('link')
    link.rel = 'icon'
    document.head.appendChild(link)
  }
  link.href = favicon
}

export const useBrandingStore = defineStore('branding', () => {
  const data = ref<TenantBranding | null>(null)
  const loaded = ref(false)

  const displayName = computed(
    () => data.value?.displayName?.trim() || data.value?.name?.trim() || DEFAULT_NAME,
  )
  const logo = computed(() => data.value?.logo || '')
  const favicon = computed(() => data.value?.favicon || '')

  async function fetch() {
    try {
      const res = (await client.get('/public/tenant/branding')) as R<TenantBranding>
      data.value = res.data ?? null
      applyDocumentMeta(displayName.value, data.value?.favicon)
    } catch {
      data.value = null
      applyDocumentMeta(DEFAULT_NAME, null)
    } finally {
      loaded.value = true
    }
    return data.value
  }

  return { data, loaded, displayName, logo, favicon, fetch }
})
