import api from '@/lib/api';
import {
  LoginRequest,
  SignupRequest,
  JwtAuthenticationResponse,
  User,
  Story,
  CreateStoryRequest,
  UpdateStoryRequest,
  PageableResponse,
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
    const response = await api.get('/stories?size=200'); // Get up to 200 stories
    return response.data.content; // Extract content array from paginated response
  },

  async getMyStories(): Promise<Story[]> {
    const response = await api.get('/stories/my?size=200'); // Get up to 200 user stories
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
