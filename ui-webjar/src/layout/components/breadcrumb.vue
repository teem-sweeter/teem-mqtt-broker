<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-for="item in breadcrumbList" :key="item.path">
      {{ item.meta.breadcrumbName || item.meta.title || item.name }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { watch, ref } from "vue";
import { useRoute } from "vue-router";
const route = useRoute();

// 过滤路由匹配列表，避免重复显示
const getBreadcrumbList = (matched) => {
  if (!matched || matched.length === 0) {
    return [];
  }
  
  // 过滤掉layout等无意义的路由，只保留有实际意义的路由项
  const validRoutes = matched.filter(item => {
    // 排除layout组件的路由（通常为父级路由）
    // 检查组件是否是Layout组件（通过组件名或路径判断）
    const isLayoutComponent = item.component && (
      typeof item.component === 'string' && item.component.includes('Layout') ||
      item.component.name && item.component.name.includes('Layout') ||
      (item.component.__file && item.component.__file && item.component.__file.includes('Layout'))
    );
    
    return item.name && item.name !== '系统信息' && item.meta && 
           (item.meta.breadcrumbName || item.meta.title || item.name) && 
           !isLayoutComponent;
  });
  
  // 检查是否有重复的面包屑名称，如果有则只保留最后一个
  const uniqueRoutes = [];
  const seenNames = new Set();
  
  // 从后往前遍历，保留最后出现的项（即最具体的路由）
  for (let i = validRoutes.length - 1; i >= 0; i--) {
    const item = validRoutes[i];
    const displayName = item.meta.breadcrumbName || item.meta.title || item.name;
    
    if (!seenNames.has(displayName)) {
      seenNames.add(displayName);
      uniqueRoutes.unshift(item); // 从前面插入以保持原始顺序
    }
  }
  
  return uniqueRoutes;
};

const breadcrumbList = ref(route.path === "/dashboard" ? [] : getBreadcrumbList(route.matched));

watch(
  () => route.matched,
  (newList) => {
    if (route.path === "/dashboard") {
      breadcrumbList.value = [];
    } else {
      breadcrumbList.value = getBreadcrumbList(newList);
    }
  }
);
</script>

<style scoped>
</style>
