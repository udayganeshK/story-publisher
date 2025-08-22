// Types for API responses
export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  bio?: string;
  profileImageUrl?: string;
  createdAt: string | number[];
  updatedAt: string | number[];
}

export interface Story {
  id: number;
  title: string;
  content: string;
  excerpt?: string;
  slug?: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  privacy: 'PUBLIC' | 'PRIVATE';
  coverImageUrl?: string;
  author: User;
  readTime: number;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  createdAt: string | number[];
  updatedAt: string | number[];
  publishedAt?: string | number[];
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface JwtAuthenticationResponse {
  accessToken: string;
  tokenType: string;
  username: string;
  email: string;
}

export interface CreateStoryRequest {
  title: string;
  content: string;
  status: 'DRAFT' | 'PUBLISHED';
  privacy: 'PUBLIC' | 'PRIVATE';
  coverImageUrl?: string;
}

export interface UpdateStoryRequest {
  title?: string;
  content?: string;
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  privacy?: 'PUBLIC' | 'PRIVATE';
  coverImageUrl?: string;
}

export interface ApiError {
  message: string;
  timestamp: string;
  status: number;
}

export interface PageableResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  numberOfElements: number;
  first: boolean;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  empty: boolean;
}

// New interfaces for image upload
export interface ImageUploadResponse {
  success: boolean;
  imageUrl?: string;
  message: string;
}

export interface ImageConfig {
  enabled: boolean;
}
