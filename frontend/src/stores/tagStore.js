import { defineStore } from 'pinia'
import * as tagApi from '@/api/tag'

export const useTagStore = defineStore('tag', {
  state: () => ({
    tags: [],
    loading: false
  }),

  actions: {
    async fetchTags() {
      this.loading = true
      try {
        const response = await tagApi.getAllTags()
        this.tags = response.data
        return response
      } finally {
        this.loading = false
      }
    },

    async createTag(data) {
      const response = await tagApi.createTag(data)
      await this.fetchTags()
      return response
    },

    async updateTag(id, data) {
      const response = await tagApi.updateTag(id, data)
      await this.fetchTags()
      return response
    },

    async deleteTag(id) {
      const response = await tagApi.deleteTag(id)
      await this.fetchTags()
      return response
    }
  }
})
