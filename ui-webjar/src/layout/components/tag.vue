<template>
  <div class="tag-container flex justify-between pl-3 pr-3 align-center">
    <div class="tag-item-container flex align-center h-p-100">
      <div
        class="tag-item flex align-center"
        :class="item.name === $route.path ? 'tag-active' : 'tag-normal'"
        v-for="(item, index) in tagList"
        :key="item.name"
        @click.stop="switchRoute(item)"
        @contextmenu.prevent="showContextMenu($event, item, index)"
      >
        <div :underline="false" class="tag-title">
          {{ item.breadcrumbName }}
        </div>
        <el-icon
          v-if="item.name !== '/dashboard'"
          :size="14"
          title="关闭窗口"
          @click.stop="closeTag(item.name, index)"
        ><Close />
        </el-icon>
      </div>
    </div>
    <!-- 右键菜单 -->
    <div
      v-if="contextMenuVisible"
      :style="{ left: contextMenuPosition.x+20 + 'px', top: contextMenuPosition.y+10 + 'px' }"
      class="context-menu"
      @mouseleave="hideContextMenu"
    >
      <ul>
        <li @click="closeCurrentTag" @mouseenter="highlightItem($event)" @mouseleave="unhighlightItem($event)">
          <span>关闭当前</span>
        </li>
        <li @click="closeOther" @mouseenter="highlightItem($event)" @mouseleave="unhighlightItem($event)">
          <span>关闭其他</span>
        </li>
        <li @click="closeAll" @mouseenter="highlightItem($event)" @mouseleave="unhighlightItem($event)">
          <span>关闭所有</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import router from "@/router";
import { useTagStore } from "@/stores/tagList.js";
import { useRoute } from "vue-router";
import { ref } from "vue";
import { Close } from "@element-plus/icons-vue";

const route = useRoute();
const tagStore = useTagStore();
const tagList = tagStore.tagList;

// 右键菜单状态
const contextMenuVisible = ref(false);
const contextMenuPosition = ref({ x: 0, y: 0 });
let currentTag = ref(null);

// 切换Tag
const switchRoute = (item) => {
  router.push(item.name);
};

// 关闭窗口
const closeTag = (path, index) => {
  tagStore.deleteTagList(index);
  if (route.path === path) {
    router.push(tagList[tagList.length - 1].name);
  }
};

// 关闭除首页外其他窗口
const closeOther = () => {
  let rawArray = tagList.find((val) => val.name === route.path);
  tagStore.closeTagOther(rawArray);
};

// 关闭除首页外所有窗口
const closeAll = () => {
  tagStore.closeAllTag();
  router.push("/dashboard");
};

// 显示右键菜单
const showContextMenu = (event, item, index) => {
  currentTag.value = { item, index };
  contextMenuVisible.value = true;
  contextMenuPosition.value = { x: event.clientX, y: event.clientY };
};

// 隐藏右键菜单
const hideContextMenu = () => {
  contextMenuVisible.value = false;
};

// 关闭当前标签
const closeCurrentTag = () => {
  if (currentTag.value) {
    closeTag(currentTag.value.item.name, currentTag.value.index);
  }
  hideContextMenu();
};

// 高亮菜单项
const highlightItem = (event) => {
  event.currentTarget.classList.add('context-menu-item-hover')
};

// 取消高亮菜单项
const unhighlightItem = (event) => {
  event.currentTarget.classList.remove('context-menu-item-hover')
};
</script>

<style scoped>
.tag-container {
  z-index: 99;
  height: 38px;
  background: var(--bg-card);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border-radius: 10px;
  border: 1px solid var(--border-light);
}

.tag-item-container {
  gap: 6px;
}

.tag-item {
  min-width: 80px;
  width: auto;
  max-width: 200px;
  height: 28px;
  border-radius: 7px;
  cursor: pointer;
  padding: 0 12px;
  gap: 6px;
  font-size: 13px;
  transition: background-color 0.25s, color 0.25s;
  white-space: nowrap;
}

.tag-title {
  text-align: center;
  display: flex;
  align-items: center;
  font-size: 13px;
}

.tag-active {
  background: var(--color-primary-gradient);
  color: #fff;
  box-shadow: 0 2px 8px var(--color-primary-shadow);
}

.tag-active .el-icon {
  color: rgba(255, 255, 255, 0.8);
}

.tag-active .el-icon:hover {
  color: #fff;
}

.tag-normal {
  background-color: var(--tag-normal-bg);
  color: var(--text-secondary);
  border: 1px solid transparent;
}

.tag-normal:hover {
  background-color: var(--color-primary-bg);
  color: var(--color-primary);
  border-color: var(--color-primary-hover);
}

.tag-normal .el-icon {
  color: var(--text-placeholder);
}

.tag-normal .el-icon:hover {
  color: var(--color-primary);
  background-color: var(--color-primary-hover);
  border-radius: 50%;
}

.context-menu {
  position: fixed;
  background: var(--context-menu-bg);
  border: 1px solid var(--border-base);
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  min-width: 130px;
  padding: 6px;
  > ul {
    list-style: none;
    padding: 0;
    margin: 0;
    > li {
      padding: 8px 14px;
      cursor: pointer;
      border-radius: 8px;
      transition: all 0.2s ease;
      font-size: 13px;
      color: var(--text-secondary);
      &:hover {
        background-color: var(--color-primary-bg);
        color: var(--color-primary);
      }
    }
  }
}
.context-menu-item-hover {
  background-color: var(--color-primary-bg) !important;
  color: var(--color-primary) !important;
}
</style>
