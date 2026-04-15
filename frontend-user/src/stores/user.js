import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const token = ref(localStorage.getItem('token') || '')

  const setUser = (info, tokenStr) => {
    userInfo.value = info
    token.value = tokenStr
    localStorage.setItem('userInfo', JSON.stringify(info))
    localStorage.setItem('token', tokenStr)
  }

  const clearUser = () => {
    userInfo.value = null
    token.value = ''
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
  }

  return { userInfo, token, setUser, clearUser }
})
