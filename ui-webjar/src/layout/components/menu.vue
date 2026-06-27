<template>
  <div class="logo-container flex-center">
    <img  width="45" src="/img/logo.png" />
    <transition name="logo-text">
      <div v-show="!isCollapse">
        <text style="font-weight: bold;margin-left: 5px">MQTT-BROKER</text>
      </div>
    </transition>
  </div>
  <div class="menu-container">
    <el-menu
      :default-active="$route.path"
      class="el-menu-vertical"
      router
      unique-opened
      :collapse="isCollapse"
      :collapse-transition="true"
      :background-color="'transparent'"
      :text-color="themeStore.isDark ? '#cfd3dc' : '#333'"
      :active-text-color="themeStore.isDark ? '#2BE08C' : '#1aab6b'"
      @select="changeMenu"
    >
      <el-menu-item index="/dashboard">
        <el-icon><PieChart/></el-icon>
        <template #title>首页</template>
      </el-menu-item>
      <template v-for="item in visibleRouterList" :key="item.name" :data-key="item.name">
        <!-- 如果子菜单只有一个，则直接渲染为一级菜单 -->
        <el-menu-item
          v-if="item.children && item.children.length === 1"
          :index="item.path + '/' + item.children[0].path"
          :key="item.path + '/' + item.children[0].path">
          <el-icon>
            <component :is="item.meta.icon" />
          </el-icon>
          <template #title>{{ item.children[0].meta.breadcrumbName }}</template>
        </el-menu-item>
        <!-- 否则渲染为子菜单 -->
        <el-sub-menu
          v-else-if="item.children && item.children.length > 0"
          :index="item.path"
          :key="item.path">
          <template #title>
            <template v-if="item.meta.icon">
              <el-icon>
                <component :is="item.meta.icon" />
              </el-icon>
            </template>
            <span :class="{'menu-title-hidden': isCollapse}">
            {{ item.meta.breadcrumbName }}
          </span>
          </template>
          <el-menu-item
            v-for="ite in item.children"
            :index="item.path + '/' + ite.path"
            :key="ite.name"
          >
            <template v-if="ite.meta.icon">
              <el-icon>
                <component :is="ite.meta.icon" />
              </el-icon>
            </template>
            {{ ite.meta.breadcrumbName }}
          </el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>
  </div>

</template>

<script setup>
import { computed } from 'vue'
import router from "@/router";
import { useAuthRouterStore } from "@/stores/authRouter.js";
import { useTagStore } from "@/stores/tagList.js";
import { useThemeStore } from "@/stores/theme.js";
import {PieChart} from "@element-plus/icons-vue";
const tagStore = useTagStore();
const themeStore = useThemeStore();
const routerOptions = router.getRoutes();

const authRouterStore = useAuthRouterStore();
const props = defineProps(["isCollapse"]);
const routerList = authRouterStore.routerList;

// 过滤掉 hidden 的菜单项
const visibleRouterList = computed(() => {
  return routerList.map(item => {
    if (!item.children || item.children.length === 0) return item
    const visibleChildren = item.children.filter(c => !c.meta?.hidden)
    return { ...item, children: visibleChildren }
  }).filter(item => !item.meta?.hidden && item.children && item.children.length > 0)
});

const changeMenu = (menu) => {
  let obj = routerOptions.find(val => val.path === menu);
  if (obj) {
    tagStore.addTagList({
      name: obj.path,
      breadcrumbName: obj.meta.breadcrumbName
    });
  }
};
</script>

<style scoped>
.el-menu-vertical:not(.el-menu--collapse) {
  width: 180px;
}
.logo-container {
  width: 100%;
  height: 60px;
  overflow: hidden;
}
.logo-icon {
  height: 60px;
  scale: 1.4;
}
.el-menu-vertical .el-menu-item span,
.el-menu-vertical .el-sub-menu__title span {
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: inline-block;
  vertical-align: middle;
  transition: max-width 0.3s ease, opacity 0.3s ease, visibility 0.3s ease;
  will-change: max-width, opacity;
  transform: translateZ(0);
}

.el-menu--collapse .el-menu-item span,
.el-menu--collapse .el-sub-menu__title span {
  max-width: 0;
  opacity: 0;
  padding: 0;
}

/* 添加logo文字过渡动画 */
.logo-text-enter-active,
.logo-text-leave-active {
  transition: opacity 0.3s ease;
  overflow: hidden;
}
.logo-text-enter-from,
.logo-text-leave-to {
  opacity: 0;
  width: 0;
}

::v-deep(.el-menu-item.is-active) {
  background-color: var(--bg-secondary);
  box-shadow: none;
  border-radius: .5rem;
  font-weight: 600;
  margin: 2px 5px ;
  color: var(--text-primary)!important;
}
::v-deep(.el-menu-item:hover) {
  background-color: var(--bg-secondary);
  margin: 2px 5px ;
  box-shadow: none;
  border-radius: .5rem;
}
::v-deep(.el-menu-item) {
  margin: 2px 5px ;
  box-shadow: none;
  border-radius: .5rem;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

::v-deep(.el-sub-menu__title) {
  margin: 2px 5px ;
  box-shadow: none;
  border-radius: .5rem;
  height: 40px;
  line-height: 40px;
  display: flex;
  align-items: center;
  padding: 0 15px;
  justify-content: flex-start;
}

/* 确保图标固定位置 */
::v-deep(.el-menu-item .el-icon),
::v-deep(.el-sub-menu__title .el-icon) {
  margin-right: 10px;
  min-width: 16px;
  text-align: center;
  flex-shrink: 0;
  transition: margin 0.3s ease;
}

/* 在折叠状态下调整图标样式 */
.el-menu--collapse ::v-deep(.el-menu-item .el-icon),
.el-menu--collapse ::v-deep(.el-sub-menu__title .el-icon) {
  margin-right: 0;
  justify-content: center;
}

</style>

<style lang="scss">
.menu-container{
  .el-menu-item,
  .el-sub-menu__title {
    margin: 5px 5px !important;
    border-radius: .5rem !important;
    height: 58px !important;
    line-height: 58px !important;
    contain: layout style paint;
    transform: translateZ(0);
    will-change: transform;
    display: flex;
    align-items: center;
    padding-left: 15px !important;
    padding-right: 15px !important;
  }

  .el-menu-item:hover,
  .el-sub-menu__title:hover {
    background-color: rgba(97, 225, 135, 0.77) !important;
    box-shadow: none !important;
  }

  .el-menu-item.is-active,
  .el-sub-menu__title.is-active {
    background-color: rgb(97, 225, 135) !important;
    box-shadow: none !important;
    font-weight: 600;
    color: #f3f3f3 !important;
  }
  .el-menu-item .el-menu-tooltip__trigger{
    padding: 0 15px;
  }
  .el-sub-menu .el-menu {
    background-color: var(--bg-submenu) !important;
    padding-top: 5px;
    padding-bottom: 5px;
  }
}

html.dark .menu-container {
  .el-menu-item:hover,
  .el-sub-menu__title:hover {
    background-color: rgba(76, 206, 115, 0.25) !important;
  }

  .el-menu-item.is-active,
  .el-sub-menu__title.is-active {
    background-color: rgba(76, 206, 115, 0.35) !important;
  }

  .el-sub-menu .el-menu {
    background-color: rgba(30, 30, 30, 0.95) !important;
  }
}
</style>
