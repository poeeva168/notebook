import { defineStore } from 'pinia'
import { login, register, getUserInfo } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null,
    isLoggedIn: false
  }),

  actions: {
    async login(credentials) {
      try {
        const response = await login(credentials)
        this.token = response.data.token
        this.user = response.data.user
        this.isLoggedIn = true
        localStorage.setItem('token', this.token)
        localStorage.setItem('user', JSON.stringify(this.user))
        return response
      } catch (error) {
        throw error
      }
    },

    async register(userInfo) {
      try {
        const response = await register(userInfo)
        return response
      } catch (error) {
        throw error
      }
    },

    async getUserInfo() {
      try {
        const response = await getUserInfo()
        this.user = response.data
        return response
      } catch (error) {
        this.logout()
        throw error
      }
    },

    logout() {
      this.user = null
      this.token = null
      this.isLoggedIn = false
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
