import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(localStorage.getItem('theme') === 'dark')

  const EXPAND = 400
  const FADE = 300

  const toggle = () => {
    const html = document.documentElement
    const newTint = isDark.value ? '#ffffff' : '#14151C'

    // 新主题蒙版：从右上角圆形半径 0 开始，高斯模糊 + 半透明叠色
    const el = document.createElement('div')
    Object.assign(el.style, {
      position: 'fixed', inset: '0', zIndex: '99999',
      backgroundColor: newTint + '50', pointerEvents: 'none',
      backdropFilter: 'blur(30px)', webkitBackdropFilter: 'blur(30px)',
      clipPath: 'circle(0% at 100% 0%)',
    })
    html.appendChild(el)

    // 水波扩散：圆形从右上角向左下角延伸
    requestAnimationFrame(() => {
      el.style.transition = `clip-path ${EXPAND}ms cubic-bezier(0.2, 0.8, 0.2, 1)`
      requestAnimationFrame(() => {
        el.style.clipPath = 'circle(150% at 100% 0%)'
      })
    })

    // 动画进行到 60% 时切换主题，扩散结束前必须完成
    setTimeout(() => { isDark.value = !isDark.value }, EXPAND * 0.6)

    // 扩散完成后淡出
    setTimeout(() => {
      el.style.transition = `opacity ${FADE}ms ease`
      requestAnimationFrame(() => { el.style.opacity = '0' })
    }, EXPAND + 50)

    setTimeout(() => el.remove(), EXPAND + FADE + 100)
  }

  watch(isDark, (val) => {
    document.documentElement.classList.toggle('dark', val)
    localStorage.setItem('theme', val ? 'dark' : 'light')
  }, { immediate: true })

  return { isDark, toggle }
})
