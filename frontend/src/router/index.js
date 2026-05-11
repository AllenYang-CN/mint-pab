import Vue from 'vue'
import VueRouter from 'vue-router'
import { getToken } from '@/utils/auth'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue')
      },
      {
        path: 'accounts',
        name: 'Accounts',
        component: () => import('@/views/account/Account.vue')
      },
      {
        path: 'transactions',
        name: 'Transactions',
        component: () => import('@/views/transaction/Transaction.vue')
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/category/Category.vue')
      },
      {
        path: 'budgets',
        name: 'Budgets',
        component: () => import('@/views/budget/Budget.vue')
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/report/Report.vue')
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'hash',
  routes
})

const whiteList = ['/login']

router.beforeEach((to, from, next) => {
  const token = getToken()
  if (token) {
    if (to.path === '/login') {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (whiteList.includes(to.path) || to.meta?.public) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
