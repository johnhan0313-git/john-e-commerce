import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import client from '@/api/client'
import type { R, TenantBranding } from '@/types'

const DEFAULT_NAME = 'John Admin'
const TITLE_SUFFIX = '运营后台'

export function applyDocumentMeta(title: string, favicon?: string | null) {
  document.title = title
  const existing = document.querySelectorAll<HTMLLinkElement>("link[rel='icon'], link[rel='shortcut icon']")
  if (!favicon) {
    existing.forEach((el) => {
      if (el.dataset.branding === '1') el.remove()
    })
    return
  }
  let link = document.querySelector<HTMLLinkElement>("link[rel='icon'][data-branding='1']")
  if (!link) {
    link = document.createElement('link')
    link.rel = 'icon'
    link.dataset.branding = '1'
    document.head.appendChild(link)
  }
  const lower = favicon.toLowerCase()
  if (lower.includes('.svg')) link.type = 'image/svg+xml'
  else if (lower.includes('.png')) link.type = 'image/png'
  else if (lower.includes('.ico')) link.type = 'image/x-icon'
  else link.type = 'image/jpeg'
  const sep = favicon.includes('?') ? '&' : '?'
  link.href = `${favicon}${sep}v=${Date.now()}`
}

export const useBrandingStore = defineStore('branding', () => {
  const data = ref<TenantBranding | null>(null)
  const loaded = ref(false)

  const displayName = computed(
    () => data.value?.displayName?.trim() || data.value?.name?.trim() || DEFAULT_NAME,
  )
  const logo = computed(() => data.value?.logo || '')
  const favicon = computed(() => data.value?.favicon || '')
  const documentTitle = computed(() => `${displayName.value} · ${TITLE_SUFFIX}`)
  const markLetter = computed(() => (displayName.value.charAt(0) || 'J').toUpperCase())

  function apply() {
    applyDocumentMeta(documentTitle.value, data.value?.favicon)
  }

  function setFrom(payload: TenantBranding | null | undefined) {
    data.value = payload ?? null
    loaded.value = true
    apply()
  }

  async function fetch() {
    const token = localStorage.getItem('admin_token')
    try {
      const res = token
        ? ((await client.get('/tenant/branding')) as R<TenantBranding>)
        : ((await client.get('/public/tenant/branding')) as R<TenantBranding>)
      setFrom(res.data)
    } catch {
      data.value = null
      applyDocumentMeta(`${DEFAULT_NAME} · ${TITLE_SUFFIX}`, null)
      loaded.value = true
    }
    return data.value
  }

  return { data, loaded, displayName, logo, favicon, documentTitle, markLetter, fetch, apply, setFrom }
})
