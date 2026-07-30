export type ToastType = 'info' | 'success' | 'error'

export function toast(message: string, type: ToastType = 'info') {
  let root = document.querySelector('.toast-root') as HTMLElement | null
  if (!root) {
    root = document.createElement('div')
    root.className = 'toast-root'
    document.body.appendChild(root)
  }
  const el = document.createElement('div')
  el.className = `toast ${type === 'info' ? '' : type}`.trim()
  el.textContent = message
  root.appendChild(el)
  setTimeout(() => {
    el.remove()
    if (root && !root.childElementCount) root.remove()
  }, 2600)
}
