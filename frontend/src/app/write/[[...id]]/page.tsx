'use client';

import React, { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Navbar from '@/components/Navbar';
import { useAuth } from '@/contexts/AuthContext';
import { Story, CreateStoryRequest, UpdateStoryRequest } from '@/types/api';
import { storyService, imageService } from '@/services/api';

const WritePage: React.FC = () => {
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    status: 'DRAFT' as 'DRAFT' | 'PUBLISHED',
    privacy: 'PUBLIC' as 'PUBLIC' | 'PRIVATE',
    coverImageUrl: '',
  });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [storyId, setStoryId] = useState<number | null>(null);
  const [imageUploadEnabled, setImageUploadEnabled] = useState(false);
  const [imageUploading, setImageUploading] = useState(false);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  
  const { isAuthenticated } = useAuth();
  const router = useRouter();
  const params = useParams();

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    // Check if we're editing an existing story
    if (params.id) {
      setIsEditing(true);
      setLoading(true);
      const id = parseInt(params.id as string);
      setStoryId(id);
      
      const loadStory = async () => {
        try {
          const story = await storyService.getStoryById(id);
          setFormData({
            title: story.title,
            content: story.content,
            status: story.status === 'ARCHIVED' ? 'DRAFT' : story.status as 'DRAFT' | 'PUBLISHED',
            privacy: story.privacy,
            coverImageUrl: story.coverImageUrl || '',
          });
          if (story.coverImageUrl) {
            setImagePreview(story.coverImageUrl);
          }
        } catch (err) {
          setError('Failed to load story for editing');
          console.error('Error loading story:', err);
        } finally {
          setLoading(false);
        }
      };

      loadStory();
    }
    
    // Check if image upload is enabled
    const checkImageConfig = async () => {
      try {
        const config = await imageService.getImageConfig();
        setImageUploadEnabled(config.enabled);
      } catch (err) {
        console.error('Failed to check image config:', err);
        setImageUploadEnabled(false);
      }
    };
    
    checkImageConfig();
  }, [isAuthenticated, router, params.id]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setImageUploading(true);
    setError(null);

    try {
      const result = await imageService.uploadImage(file);
      if (result.success && result.imageUrl) {
        setFormData({
          ...formData,
          coverImageUrl: result.imageUrl,
        });
        setImagePreview(result.imageUrl);
      } else {
        setError(result.message || 'Failed to upload image');
      }
    } catch (err) {
      setError('Failed to upload image. Please try again.');
      console.error('Image upload error:', err);
    } finally {
      setImageUploading(false);
    }
  };

  const handleRemoveImage = () => {
    setFormData({
      ...formData,
      coverImageUrl: '',
    });
    setImagePreview(null);
  };

  const handleSave = async (publish = false) => {
    setSaving(true);
    setError(null);

    try {
      const storyData = {
        ...formData,
        status: publish ? 'PUBLISHED' as const : formData.status,
      };

      let result: Story;
      if (isEditing && storyId) {
        result = await storyService.updateStory(storyId, storyData as UpdateStoryRequest);
      } else {
        result = await storyService.createStory(storyData as CreateStoryRequest);
      }

      if (publish) {
        router.push(`/story/${result.id}`);
      } else {
        router.push('/dashboard');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save story. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleSave(false);
  };

  const handlePublish = () => {
    handleSave(true);
  };

  if (!isAuthenticated) {
    return null; // Will redirect to login
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-4xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <p className="mt-2 text-gray-500">Loading story...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      
      <div className="max-w-4xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
        <div className="bg-white shadow-sm rounded-lg">
          <div className="px-6 py-4 border-b border-gray-200">
            <h1 className="text-2xl font-bold text-gray-900">
              {isEditing ? 'Edit Story' : 'Write New Story'}
            </h1>
          </div>
          
          <form onSubmit={handleSubmit} className="p-6 space-y-6">
            {/* Title */}
            <div>
              <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-2">
                Title
              </label>
              <input
                type="text"
                id="title"
                name="title"
                required
                placeholder="Enter your story title..."
                value={formData.title}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 text-lg text-gray-900"
              />
            </div>

            {/* Content */}
            <div>
              <label htmlFor="content" className="block text-sm font-medium text-gray-700 mb-2">
                Content
              </label>
              <textarea
                id="content"
                name="content"
                required
                rows={20}
                placeholder="Tell your story..."
                value={formData.content}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 resize-y text-gray-900 text-base leading-relaxed"
              />
            </div>

            {/* Cover Image Upload */}
            {imageUploadEnabled && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Cover Image
                </label>
                <div className="flex items-center space-x-4">
                  {imagePreview ? (
                    <div className="relative">
                      <img
                        src={imagePreview}
                        alt="Cover Image Preview"
                        className="w-full h-auto rounded-md border border-gray-300"
                      />
                      <button
                        type="button"
                        onClick={handleRemoveImage}
                        className="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full hover:bg-red-600 transition-colors"
                        title="Remove image"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                    </div>
                  ) : (
                    <div className="text-center text-gray-500">
                      No image uploaded
                    </div>
                  )}
                </div>
                
                <div className="flex items-center space-x-3 mt-4">
                  <label className="block text-sm font-medium text-gray-700">
                    <span className="sr-only">Choose cover image</span>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleImageUpload}
                      className="block w-full text-sm text-gray-900 file:py-2 file:px-4 file:border file:border-gray-300 file:rounded-md file:text-sm file:font-medium file:bg-gray-50 hover:file:bg-gray-100 transition-colors"
                    />
                  </label>
                  
                  {imageUploading && (
                    <div className="text-sm text-gray-500">
                      Uploading image...
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Settings */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label htmlFor="privacy" className="block text-sm font-medium text-gray-700 mb-2">
                  Privacy
                </label>
                <select
                  id="privacy"
                  name="privacy"
                  value={formData.privacy}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 text-gray-900"
                >
                  <option value="PUBLIC">Public</option>
                  <option value="PRIVATE">Private</option>
                </select>
              </div>
              
              <div>
                <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-2">
                  Status
                </label>
                <select
                  id="status"
                  name="status"
                  value={formData.status}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 text-gray-900"
                >
                  <option value="DRAFT">Draft</option>
                  <option value="PUBLISHED">Published</option>
                </select>
              </div>
            </div>

            {error && (
              <div className="rounded-md bg-red-50 p-4">
                <div className="text-sm text-red-700">{error}</div>
              </div>
            )}

            {/* Actions */}
            <div className="flex items-center justify-between pt-4">
              <button
                type="button"
                onClick={() => router.push('/dashboard')}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
              >
                Cancel
              </button>
              
              <div className="flex space-x-3">
                <button
                  type="submit"
                  disabled={saving}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {saving ? 'Saving...' : 'Save Draft'}
                </button>
                
                <button
                  type="button"
                  onClick={handlePublish}
                  disabled={saving}
                  className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {saving ? 'Publishing...' : 'Publish Story'}
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default WritePage;
