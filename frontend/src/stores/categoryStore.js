import { defineStore } from 'pinia'
import * as categoryApi from '@/api/category'

export const useCategoryStore = defineStore('category', {
  state: () => ({
    categories: [],
    loading: false
  }),

  actions: {
    async fetchCategories() {
      this.loading = true
      try {
        const response = await categoryApi.getCategoryTree()
        this.categories = response.data
        return response
      } finally {
        this.loading = false
      }
    },

    async createCategory(data) {
      const response = await categoryApi.createCategory(data)
      await this.fetchCategories()
      return response
    },

    async updateCategory(id, data) {
      const response = await categoryApi.updateCategory(id, data)
      await this.fetchCategories()
      return response
    },

    async deleteCategory(id) {
      const response = await categoryApi.deleteCategory(id)
      await this.fetchCategories()
      return response
    }
  }
})
