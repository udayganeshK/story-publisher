import api from '@/lib/api';
import {
  LoginRequest,
  SignupRequest,
  JwtAuthenticationResponse,
  Story,
  Category,
  CreateStoryRequest,
  UpdateStoryRequest,
  Translation,
  TranslationResponse,
} from '@/types/api';

export const authService = {
  async login(credentials: LoginRequest): Promise<JwtAuthenticationResponse> {
    const response = await api.post('/auth/login', credentials);
    return response.data;
  },

  async signup(userData: SignupRequest): Promise<JwtAuthenticationResponse> {
    const response = await api.post('/auth/signup', userData);
    return response.data;
  },
};

export const storyService = {
  async getAllPublicStories(page: number = 0, size: number = 100): Promise<{ content: Story[], totalElements: number, totalPages: number }> {
    const response = await api.get(`/stories?page=${page}&size=${size}`);
    return response.data; // Return full paginated response
  },

  async getAllPublicStoriesSimple(): Promise<Story[]> {
    const response = await api.get('/stories?size=500'); // Get up to 500 stories
    return response.data.content; // Extract content array from paginated response
  },

  async getMyStories(): Promise<Story[]> {
    const response = await api.get('/stories/my?size=500'); // Get up to 500 user stories
    return response.data.content; // Extract content array from paginated response
  },

  async getStoryById(id: number): Promise<Story> {
    const response = await api.get(`/stories/${id}`);
    return response.data;
  },

  async getStoryBySlugOrId(slugOrId: string): Promise<Story> {
    // Always treat as ID for simplicity
    const response = await api.get(`/stories/${slugOrId}`);
    return response.data;
  },

  async createStory(storyData: CreateStoryRequest): Promise<Story> {
    const response = await api.post('/stories', storyData);
    return response.data;
  },

  async updateStory(id: number, storyData: UpdateStoryRequest): Promise<Story> {
    const response = await api.put(`/stories/${id}`, storyData);
    return response.data;
  },

  async deleteStory(id: number): Promise<void> {
    await api.delete(`/stories/${id}`);
  },

  async publishStory(id: number): Promise<Story> {
    const response = await api.post(`/stories/${id}/publish`);
    return response.data;
  },

  async unpublishStory(id: number): Promise<Story> {
    const response = await api.post(`/stories/${id}/unpublish`);
    return response.data;
  },
  async getStoriesByCategory(categoryId: number, page: number = 0, size: number = 10): Promise<{ content: Story[], totalElements: number, totalPages: number }> {
    const response = await api.get(`/stories/category/${categoryId}?page=${page}&size=${size}`);
    return response.data;
  },

  async updateStoryCategory(id: number, categoryId: number | null): Promise<Story> {
    const params = categoryId ? `?categoryId=${categoryId}` : '';
    const response = await api.put(`/stories/${id}/category${params}`);
    return response.data;
  },
};

export const categoryService = {
  async getAllCategories(): Promise<Category[]> {
    const response = await api.get('/categories');
    return response.data;
  },

  async getCategoryById(id: number): Promise<Category> {
    const response = await api.get(`/categories/${id}`);
    return response.data;
  },

  async getCategoryBySlug(slug: string): Promise<Category> {
    const response = await api.get(`/categories/slug/${slug}`);
    return response.data;
  },
};

export const imageService = {
  async getImageConfig(): Promise<{ enabled: boolean }> {
    const response = await api.get('/images/config');
    return response.data;
  },

  async uploadImage(file: File): Promise<{ success: boolean; imageUrl?: string; message: string }> {
    const formData = new FormData();
    formData.append('image', file);
    
    const response = await api.post('/images/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
};

export const translationService = {
  async translateText(text: string, sourceLanguage?: string, targetLanguage: string = 'en'): Promise<TranslationResponse> {
    const response = await api.post('/translations/text', {
      text,
      sourceLanguage,
      targetLanguage
    });
    return response.data;
  },

  async translateStory(storyId: number, targetLanguage: string = 'en'): Promise<{ translationId: number; sourceLanguage: string; targetLanguage: string; translatedContent: string; storyId: number }> {
    const response = await api.post(`/translations/story/${storyId}`, {
      targetLanguage
    });
    return response.data;
  },

  async getStoryTranslations(storyId: number): Promise<{ translations: Translation[]; count: number }> {
    const response = await api.get(`/translations/story/${storyId}`);
    return response.data;
  },

  async getSupportedLanguages(): Promise<{ supportedLanguages: string[]; languageNames: Record<string, string> }> {
    const response = await api.get('/translations/languages');
    return response.data;
  },

  async detectLanguage(text: string): Promise<{ detectedLanguage: string; text: string }> {
    const response = await api.post('/translations/detect', { text });
    return response.data;
  },
};
