import { createRouter, createWebHistory, createWebHashHistory } from 'vue-router'
import Layout from '../layout/index.vue'
import { getAuthRouters } from "@/router/authRouter";
import {getDynamicRoutes} from '@/router/menus.js'
import { useAuthRouterStore } from '@/stores/authRouter.js'
import {check} from "@/api/login.js"

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'systemInfo',
      component: Layout,
      redirect: '/dashboard',
      meta: {
        staticRouter: true
      },
      children: [{
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/Dashboard/index.vue'),
        meta: {
          breadcrumbName: 'breadcrumb.systemInfo',
          icon: 'dashboard',
          staticRouter: true // 静态路由
        }
      }, ]
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login/index.vue'),
    }
  ],
})

// 验证token
router.beforeEach(async(to, from, next) => {
  const response = await check();
  let {initialSetup} = response;
  if (initialSetup && to.path !== '/initialSetup') {
    next('/initialSetup');
  }else if (!response.initialSetup) {
    const token = sessionStorage.token;
    if (to.path !== '/login' && !token) {
      next('/login');
    } else {
      const authRouterStore = useAuthRouterStore()
      let {routerList} = authRouterStore
      if (!routerList.length) {
        let dynamicRoutes = await getDynamicRoutes();
        // 动态添加路由
        let newRoutes = getAuthRouters(dynamicRoutes);
        authRouterStore.addRouterList(newRoutes);
        newRoutes.forEach(val => {
          router.addRoute(val);
        });
        next({path: to.path});
      } else {
        next();
      }
    }
  }else {
    next();
  }
})

export default router
