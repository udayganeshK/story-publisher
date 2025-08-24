'use client';

import React, { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Navbar from '@/components/Navbar';
import TranslationComponent from '@/components/TranslationComponent';
import { useAuth } from '@/contexts/AuthContext';
import { Story, Category } from '@/types/api';
import { storyService, categoryService } from '@/services/api';

const StoryPage: React.FC = () => {
  const [story, setStory] = useState<Story | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [showCategorySelector, setShowCategorySelector] = useState(false);
  const [categoryUpdateLoading, setCategoryUpdateLoading] = useState(false);
  
  const { isAuthenticated, user } = useAuth();
  const params = useParams();
  const router = useRouter();

  useEffect(() => {
    const loadStory = async () => {
      try {
        if (params.slug) {
          console.log('Attempting to load story with slug/id:', params.slug);
          const storyData = await storyService.getStoryBySlugOrId(params.slug as string);
          console.log('Successfully loaded story:', storyData.title);
          setStory(storyData);
        }
      } catch (err: any) {
        console.error('Error loading story:', err);
        console.error('Error details:', {
          status: err.response?.status,
          data: err.response?.data,
          slug: params.slug
        });
        
        if (err.response?.status === 404) {
          setError('Story not found. This story may have been deleted or is not published yet.');
        } else if (err.response?.status === 403) {
          setError('This story is private and cannot be viewed.');
        } else {
          setError('Failed to load story. Please try again later.');
        }
      } finally {
        setLoading(false);
      }
    };

    const loadCategories = async () => {
      if (isAuthenticated) {
        try {
          const categoriesData = await categoryService.getAllCategories();
          setCategories(categoriesData);
        } catch (err) {
          console.error('Failed to load categories:', err);
        }
      }
    };

    loadStory();
    loadCategories();
  }, [params.slug, isAuthenticated]);

  const handleCategoryUpdate = async (categoryId: number | null) => {
    if (!story || !isAuthenticated) return;
    
    setCategoryUpdateLoading(true);
    try {
      const updatedStory = await storyService.updateStoryCategory(story.id, categoryId);
      setStory(updatedStory);
      setShowCategorySelector(false);
    } catch (err) {
      console.error('Failed to update category:', err);
      alert('Failed to update category. Please try again.');
    } finally {
      setCategoryUpdateLoading(false);
    }
  };

  const formatDate = (dateInput: string | number[]) => {
    let date: Date;
    
    if (Array.isArray(dateInput) && dateInput.length >= 3) {
      // Backend returns date as array: [year, month, day, hour, minute, second, nanosecond]
      const [year, month, day, hour = 0, minute = 0, second = 0] = dateInput;
      date = new Date(year, month - 1, day, hour, minute, second); // month is 0-indexed in JS
    } else if (typeof dateInput === 'string') {
      date = new Date(dateInput);
    } else {
      // Fallback to current date if invalid format
      date = new Date();
    }
    
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const formatContent = (content: string) => {
    return content.split('\n').map((paragraph, index) => (
      <p key={index} className="mb-4 text-gray-800 leading-relaxed">
        {paragraph}
      </p>
    ));
  };

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

  if (error || !story) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-4xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <div className="mb-4">
              <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">{error}</h2>
            <p className="text-gray-600 mb-6">The story you're looking for might have been moved or deleted.</p>
            <button
              onClick={() => router.push('/')}
              className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-md text-sm font-medium transition-colors"
            >
              Go back home
            </button>
          </div>
        </div>
      </div>
    );
  }

  const isOwner = isAuthenticated && story.author.id === user?.id;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      
      <article className="max-w-4xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
        <div className="bg-white shadow-sm rounded-lg overflow-hidden">
          {/* Cover Image */}
          {story.coverImageUrl && (
            <div className="w-full h-64 md:h-80 relative bg-gray-100 cursor-pointer group overflow-hidden" onClick={() => window.open(story.coverImageUrl, '_blank')}>
              <img
                src={story.coverImageUrl}
                alt={story.title}
                className="w-full h-full object-contain transition-opacity group-hover:opacity-90"
                loading="lazy"
              />
              <div className="absolute top-2 right-2 bg-black bg-opacity-50 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity">
                Click to view full size
              </div>
            </div>
          )}
          
          {/* Header */}
          <div className="px-8 py-6 border-b border-gray-200">
            <div className="flex items-center space-x-4 mb-4">
              <div className="flex-shrink-0">
                <div className="h-10 w-10 rounded-full bg-blue-500 flex items-center justify-center">
                  <span className="text-white font-medium">
                    {story.author?.firstName?.[0] || story.author?.username?.[0] || 'U'}
                    {story.author?.lastName?.[0] || ''}
                  </span>
                </div>
              </div>
              <div>
                <p className="text-sm font-medium text-gray-900">
                  {story.author?.firstName && story.author?.lastName 
                    ? `${story.author.firstName} ${story.author.lastName}`
                    : story.author?.username || 'Unknown Author'}
                </p>
                <div className="flex items-center text-sm text-gray-500 space-x-2">
                  <span>{formatDate(story.publishedAt || story.createdAt)}</span>
                  <span>•</span>
                  <span>{story.readTime || 1} min read</span>
                  {story.category && (
                    <>
                      <span>•</span>
                      <span 
                        className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium text-white"
                        style={{ backgroundColor: story.category.color || '#6B7280' }}
                      >
                        {story.category.name}
                      </span>
                    </>
                  )}
                  {story.privacy === 'PRIVATE' && (
                    <>
                      <span>•</span>
                      <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">
                        Private
                      </span>
                    </>
                  )}
                </div>
              </div>
            </div>
            
            <h1 className="text-3xl sm:text-4xl font-bold text-gray-900 leading-tight">
              {story.title}
            </h1>
          </div>

          {/* Content */}
          <div className="px-8 py-8">
            {/* Translation Component */}
            <div className="mb-6 p-5 bg-gradient-to-r from-gray-50 to-blue-50 rounded-xl border border-gray-200 shadow-sm">
              <h3 className="text-base font-semibold text-gray-800 mb-4 flex items-center gap-2">
                <span className="text-xl">🌍</span>
                Translation & Language Detection
              </h3>
              <TranslationComponent 
                text={story.content}
                className="w-full"
              />
            </div>

            {/* Category Assignment (Only for story owner) */}
            {isOwner && (
              <div className="mb-6 p-4 bg-blue-50 rounded-lg border border-blue-200">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-sm font-medium text-gray-700 mb-1">📝 Category Management</h3>
                    <p className="text-xs text-gray-600">
                      Current: {story.category?.name || 'Auto-categorized'}
                      {story.category && (
                        <span 
                          className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium text-white ml-2"
                          style={{ backgroundColor: story.category.color || '#6B7280' }}
                        >
                          {story.category.name}
                        </span>
                      )}
                    </p>
                  </div>
                  <button
                    onClick={() => setShowCategorySelector(!showCategorySelector)}
                    className="px-3 py-1.5 text-sm font-medium text-blue-700 bg-blue-100 hover:bg-blue-200 rounded-md transition-colors"
                    disabled={categoryUpdateLoading}
                  >
                    {showCategorySelector ? 'Cancel' : 'Change Category'}
                  </button>
                </div>
                
                {showCategorySelector && (
                  <div className="mt-4 space-y-3">
                    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2">
                      <button
                        onClick={() => handleCategoryUpdate(null)}
                        disabled={categoryUpdateLoading}
                        className="p-2 text-xs text-center border border-gray-300 rounded-md hover:bg-gray-50 transition-colors disabled:opacity-50"
                      >
                        Auto-categorize
                      </button>
                      {categories.map((category) => (
                        <button
                          key={category.id}
                          onClick={() => handleCategoryUpdate(category.id)}
                          disabled={categoryUpdateLoading}
                          className="p-2 text-xs text-center text-white rounded-md hover:opacity-80 transition-opacity disabled:opacity-50"
                          style={{ backgroundColor: category.color || '#6B7280' }}
                        >
                          {category.name}
                        </button>
                      ))}
                    </div>
                    {categoryUpdateLoading && (
                      <p className="text-xs text-blue-600">Updating category...</p>
                    )}
                  </div>
                )}
              </div>
            )}
            
            <div className="prose prose-lg max-w-none">
              {formatContent(story.content)}
            </div>
          </div>

          {/* Footer */}
          <div className="px-8 py-6 border-t border-gray-200 bg-gray-50">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-6">
                <span className="flex items-center text-sm text-gray-500">
                  <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" clipRule="evenodd" />
                  </svg>
                  {story.likeCount || 0} {(story.likeCount || 0) === 1 ? 'like' : 'likes'}
                </span>
                <span className="flex items-center text-sm text-gray-500">
                  <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z" clipRule="evenodd" />
                  </svg>
                  {story.commentCount || 0} {(story.commentCount || 0) === 1 ? 'comment' : 'comments'}
                </span>
              </div>
              
              <div className="flex items-center space-x-3">
                <button className="flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors">
                  <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.367 2.684 3 3 0 00-5.367-2.684z" />
                  </svg>
                  Share
                </button>
                <button className="flex items-center px-4 py-2 text-sm font-medium text-red-600 bg-white border border-red-300 rounded-md hover:bg-red-50 transition-colors">
                  <svg className="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" clipRule="evenodd" />
                  </svg>
                  Like
                </button>
              </div>
            </div>
          </div>
        </div>
      </article>
    </div>
  );
};

export default StoryPage;
