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
  async getAllPublicStories(): Promise<Story[]> {
    const response = await api.get('/stories');
    return response.data.content; // Extract content array from paginated response
  },

  async getMyStories(): Promise<Story[]> {
    const response = await api.get('/stories/my');
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
