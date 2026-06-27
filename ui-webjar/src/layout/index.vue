<template>
  <div class="common-layout h-p-100 w-p-100">
    <el-container class="h-p-100 w-p-100">
      <el-aside
        :width="isCollapse ? '65px' : '180px'"
        class="common-aside"
      >
        <MyMenu :isCollapse="isCollapse"></MyMenu>
      </el-aside>
      <el-container>
        <el-header class="common-header flex align-center">
          <div class="header-left flex align-center">
            <div class="collapse-btn" @click="changeCollapse">
              <el-icon :size="20" style="color: var(--color-primary)">
                <Expand v-if="isCollapse" />
                <Fold v-else />
              </el-icon>
            </div>
            <Breadcrumb class="ml-10"></Breadcrumb>
          </div>
          <div class="header-right flex align-center">
            <div class="theme-btn" :class="{ 'is-dark': themeStore.isDark }" @click="themeStore.toggle()" :title="themeStore.isDark ? '切换亮色' : '切换暗色'">
              <span class="theme-icon"><el-icon size="14"><Moon /></el-icon></span>
              <span class="theme-icon"><el-icon size="14"><Sunny /></el-icon></span>
              <span class="theme-thumb"></span>
            </div>
            <div class="fullscreen-btn flex align-center justify-center" @click="toggleFullscreen" :title="isFullscreen ? '退出全屏' : '全屏'">
              <el-icon size="18"><FullScreen v-if="!isFullscreen" /><Aim v-else /></el-icon>
            </div>
            <Profile></Profile>
          </div>
        </el-header>
        <el-main class="main-wrapper">
          <Tag class="my-tag"></Tag>
          <div class="flex1 common-main" v-if="alive">
            <router-view></router-view>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>
<script setup>
import { ref, watchEffect, nextTick, onMounted } from "vue";
import { useTagStore } from "@/stores/tagList.js";
import { useThemeStore } from "@/stores/theme.js";
import { FullScreen, Aim, Moon, Sunny } from '@element-plus/icons-vue';
const tagStore = useTagStore();
const themeStore = useThemeStore();
import MyMenu from "./components/menu.vue";
import Tag from "./components/tag.vue";
import Breadcrumb from "./components/breadcrumb.vue";
import Profile from "./components/profile.vue";
const isCollapse = ref(false);
const alive = ref(true)
const isFullscreen = ref(false)

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

onMounted(() => {
  document.addEventListener('fullscreenchange', () => {
    isFullscreen.value = !!document.fullscreenElement
  })
})
watchEffect(() => {
  alive.value = tagStore.alive
})

const changeCollapse = async () => {
  // 添加过渡类以优化动画性能
  const asideEl = document.querySelector('.common-aside');
  if (asideEl) {
    asideEl.style.transition = 'width 0.3s ease-in-out';
  }

  isCollapse.value = !isCollapse.value;

  // 确保DOM更新后再执行其他操作
  await nextTick();
};
</script>
<style scoped>
.common-aside {
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
  z-index: 1;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: width;
  flex-shrink: 0;
  transform: translateZ(0);
  -webkit-transform: translateZ(0);
  backface-visibility: hidden;
  perspective: 1000px;
  contain: layout style paint;
  position: relative;
  border-right: 1px solid var(--border-base);
  overflow-x: hidden;
  overflow-y: auto;
}

.common-header {
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border-base);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-right {
  margin-left: auto;
  gap: 10px;
}

.fullscreen-btn {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  color: var(--text-secondary);
}

.fullscreen-btn:hover {
  background: var(--color-primary-bg);
  border-color: var(--color-primary);
  color: var(--color-primary);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px var(--color-primary-shadow);
}

/* 胶囊式主题切换开关 */
.theme-btn {
  position: relative;
  width: 56px;
  height: 28px;
  border-radius: 999px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-base);
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 0 3px;
  gap: 0;
}

.theme-icon {
  position: relative;
  z-index: 1;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--text-muted);
}

.theme-btn:not(.is-dark) .theme-icon:first-child {
  color: var(--text-muted);
}
.theme-btn:not(.is-dark) .theme-icon:last-child {
  color: var(--text-primary);
}
.theme-btn.is-dark .theme-icon:first-child {
  color: var(--text-primary);
}
.theme-btn.is-dark .theme-icon:last-child {
  color: var(--text-muted);
}

.theme-thumb {
  position: absolute;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #ffffff4f;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  left: 3px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
}

.theme-btn.is-dark .theme-thumb {
  background: #ffffff4f;
  transform: translate(22px, -50%);
}

.collapse-btn {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background: transparent;
  border: 1px solid transparent;
  color: var(--color-primary);
}

.collapse-btn:hover {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-shadow);
  transform: scale(1.05);
}

.common-main {
  margin-top: 8px;
  border-radius: 8px;
  min-height: 0;
  background: var(--bg-secondary);
  box-shadow: var(--shadow-lg);
  overflow-y: auto;
}

.main-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 0;
  overflow: hidden;
}

.my-tag {
  flex-shrink: 0;
}

/* 深度选择器，确保面包屑样式也适配白色背景 */
.common-header :deep(.el-breadcrumb__item) {
  color: var(--text-muted);
}

.common-header :deep(.el-breadcrumb__item:last-child) {
  color: var(--text-regular);
  font-weight: 600;
}

.common-header :deep(.el-breadcrumb__separator) {
  color: var(--text-muted);
}
</style>
