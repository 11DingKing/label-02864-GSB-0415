import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/exam',
    children: [
      {
        path: 'exam',
        name: 'ExamList',
        component: () => import('../views/ExamList.vue')
      },
      {
        path: 'exam/:id',
        name: 'TakeExam',
        component: () => import('../views/TakeExam.vue')
      },
      {
        path: 'records',
        name: 'MyRecords',
        component: () => import('../views/MyRecords.vue')
      },
      {
        path: 'record/:id',
        name: 'RecordDetail',
        component: () => import('../views/RecordDetail.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
