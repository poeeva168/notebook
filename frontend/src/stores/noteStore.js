import { defineStore } from 'pinia'
import * as noteApi from '@/api/note'

export const useNoteStore = defineStore('note', {
  state: () => ({
    notes: [],
    currentNote: null,
    pagination: {
      page: 1,
      size: 20,
      total: 0
    },
    loading: false
  }),

  actions: {
    async fetchNotes(params = {}) {
      this.loading = true
      try {
        const response = await noteApi.getNoteList(params)
        this.notes = response.data.records
        this.pagination = {
          page: response.data.page,
          size: response.data.size,
          total: response.data.total
        }
        return response
      } finally {
        this.loading = false
      }
    },

    async fetchNoteById(id) {
      this.loading = true
      try {
        const response = await noteApi.getNoteById(id)
        this.currentNote = response.data
        return response
      } finally {
        this.loading = false
      }
    },

    async createNote(data) {
      const response = await noteApi.createNote(data)
      return response
    },

    async updateNote(id, data) {
      const response = await noteApi.updateNote(id, data)
      return response
    },

    async deleteNote(id) {
      const response = await noteApi.deleteNote(id)
      return response
    },

    async archiveNote(id, isArchived) {
      const response = await noteApi.archiveNote(id, isArchived)
      return response
    }
  }
})
