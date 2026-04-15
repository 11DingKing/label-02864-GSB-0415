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
        component: () => import('../views/exam/ExamList.vue')
      },
      {
        path: 'exam/create',
        name: 'ExamCreate',
        component: () => import('../views/exam/ExamForm.vue')
      },
      {
        path: 'exam/edit/:id',
        name: 'ExamEdit',
        component: () => import('../views/exam/ExamForm.vue')
      },
      {
        path: 'exam/:id/questions',
        name: 'QuestionManage',
        component: () => import('../views/exam/QuestionManage.vue')
      },
      {
        path: 'exam/:id/records',
        name: 'ExamRecords',
        component: () => import('../views/exam/ExamRecords.vue')
      },
      {
        path: 'student',
        name: 'StudentList',
        component: () => import('../views/student/StudentList.vue')
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
