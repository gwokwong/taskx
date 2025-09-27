import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/projects',
      name: 'Projects',
      component: () => import('@/views/Projects.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/projects/:id',
      name: 'ProjectDetail',
      component: () => import('@/views/ProjectDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/tasks',
      name: 'Tasks',
      component: () => import('@/views/TaskManagement.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/calendar',
      name: 'Calendar',
      component: () => import('@/views/Calendar.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/task-templates',
      name: 'TaskTemplates',
      component: () => import('@/views/TaskTemplates.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/messages',
      name: 'Messages',
      component: () => import('@/views/Messages.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/files',
      name: 'Files',
      component: () => import('@/views/Files.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/settings',
      name: 'Settings',
      component: () => import('@/views/Settings.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'Admin',
      children: [
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/Users.vue'),
          meta: { requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'departments',
          name: 'AdminDepartments',
          component: () => import('@/views/admin/Departments.vue'),
          meta: { requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'logs',
          name: 'AdminLogs',
          component: () => import('@/views/admin/Logs.vue'),
          meta: { requiresAuth: true, requiresAdmin: true }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue')
    }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next('/dashboard')
  } else if (to.meta.requiresAdmin && authStore.user?.role !== 'admin') {
    next('/dashboard')
  } else {
    next()
  }
})

export default router