import { api } from './api'

export interface SearchResult {
  type: string
  id: number
  title: string
  description: string
  url: string
  icon: string
  updatedAt: string
  matchField: string
  highlight: string
  relevanceScore: number
}

export const searchApi = {
  async globalSearch(keyword: string, type?: string): Promise<SearchResult[]> {
    const response = await api.get('/search', {
      params: { keyword, type }
    })
    return response.data.data
  },

  async searchProjects(keyword: string): Promise<SearchResult[]> {
    const response = await api.get('/search/projects', {
      params: { keyword }
    })
    return response.data.data
  },

  async searchTasks(keyword: string): Promise<SearchResult[]> {
    const response = await api.get('/search/tasks', {
      params: { keyword }
    })
    return response.data.data
  },

  async searchFiles(keyword: string): Promise<SearchResult[]> {
    const response = await api.get('/search/files', {
      params: { keyword }
    })
    return response.data.data
  },

  async searchUsers(keyword: string): Promise<SearchResult[]> {
    const response = await api.get('/search/users', {
      params: { keyword }
    })
    return response.data.data
  },

  async getSearchSuggestions(keyword: string): Promise<string[]> {
    const response = await api.get('/search/suggestions', {
      params: { keyword }
    })
    return response.data.data
  },

  async getRecentSearches(): Promise<string[]> {
    const response = await api.get('/search/recent')
    return response.data.data
  }
}

export default searchApi