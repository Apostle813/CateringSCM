import { createRouter, createWebHistory } from 'vue-router'
// 引入布局组件
import Layout from '@/layout/index.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/login',
            name: 'Login',
            component: () => import('@/views/Login.vue')
        },
        {
            path: '/',
            name: '系统主页',
            component: Layout, // 访问根路径时，加载 Layout 骨架
            redirect: '/dashboard', // 自动跳转到大屏页
            children: [
                {
                    path: 'dashboard',
                    name: '首页看板',
                    component: () => import('@/views/Dashboard.vue')
                },
                {
                    path: 'material',
                    name: '食材档案管理',
                    component: () => import('@/views/Material.vue')
                },
                {
                    path: 'purchase',
                    name: '采购订单审批',
                    component: () => import('@/views/Purchase.vue')
                },
                {
                    path: 'inventory',
                    name: '库存台账与作业',
                    component: () => import('@/views/Inventory.vue')
                },
                {
                    path: 'stocklog',
                    name: '库存流水追溯',
                    component: () => import('@/views/StockLog.vue')
                }
            ]
        },
        // 404 兜底
        {
            path: '/:pathMatch(.*)*',
            redirect: '/'
        }
    ]
})

// 路由守卫拦截保持不变
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('scm_token')
    if (to.path !== '/login' && !token) {
        next('/login')
    } else {
        next()
    }
})

export default router