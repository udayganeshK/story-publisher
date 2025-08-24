'use client';

import React, { useState, useEffect, useMemo } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import Navbar from '@/components/Navbar';
import StoryFilters, { FilterOptions } from '@/components/StoryFilters';
import { useAuth } from '@/contexts/AuthContext';
import { Story, Category } from '@/types/api';
import { storyService, categoryService } from '@/services/api';
import { processStories, formatDateShort } from '@/utils/storyFilters';

const DashboardPage: React.FC = () => {
  const [allStories, setAllStories] = useState<Story[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { user, isAuthenticated } = useAuth();
  const router = useRouter();
  const [filters, setFilters] = useState<FilterOptions>({
    search: '',
    categoryId: null,
    sortBy: 'newest',
    dateRange: 'all',
    customStartDate: '',
    customEndDate: '',
  });

  // Process stories based on current filters
  const filteredAndSortedStories = useMemo(() => {
    return processStories(allStories, filters);
  }, [allStories, filters]);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    const fetchData = async () => {
      try {
        // Fetch categories
        const categoriesData = await categoryService.getAllCategories();
        setCategories(categoriesData);

        // Fetch my stories
        const myStories = await storyService.getMyStories();
        setAllStories(Array.isArray(myStories) ? myStories : []);
      } catch (err) {
        setError('Failed to load your stories');
        console.error('Error fetching stories:', err);
        setAllStories([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isAuthenticated, router]);

  const handleDeleteStory = async (storyId: number) => {
    if (!confirm('Are you sure you want to delete this story?')) {
      return;
    }

    try {
      await storyService.deleteStory(storyId);
      setAllStories(allStories.filter(story => story.id !== storyId));
    } catch (err) {
      console.error('Error deleting story:', err);
      alert('Failed to delete story');
    }
  };

  const handleTogglePublish = async (story: Story) => {
    try {
      let updatedStory;
      if (story.status === 'PUBLISHED') {
        updatedStory = await storyService.unpublishStory(story.id);
      } else {
        updatedStory = await storyService.publishStory(story.id);
      }
      
      setAllStories(allStories.map(s => s.id === story.id ? updatedStory : s));
    } catch (err) {
      console.error('Error toggling publish status:', err);
      alert('Failed to update story status');
    }
  };

  const handleCategoryChange = async (storyId: number, categoryId: number | null) => {
    try {
      const updatedStory = await storyService.updateStoryCategory(storyId, categoryId);
      setAllStories(allStories.map(s => s.id === storyId ? updatedStory : s));
    } catch (err) {
      console.error('Error updating story category:', err);
      alert('Failed to update story category');
    }
  };

  const getStatusBadge = (status: string) => {
    const baseClasses = "px-2 py-1 text-xs font-medium rounded-full";
    switch (status) {
      case 'PUBLISHED':
        return `${baseClasses} bg-green-100 text-green-800`;
      case 'DRAFT':
        return `${baseClasses} bg-yellow-100 text-yellow-800`;
      case 'ARCHIVED':
        return `${baseClasses} bg-gray-100 text-gray-800`;
      default:
        return `${baseClasses} bg-gray-100 text-gray-800`;
    }
  };

  if (!isAuthenticated) {
    return null; // Will redirect to login
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      
      <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">
            Welcome back, {user?.firstName}!
          </h1>
          <p className="mt-2 text-gray-600">
            Manage your stories and see how they&apos;re performing.
          </p>
        </div>

        {/* Quick Actions */}
        <div className="mb-6">
          <div className="flex justify-between items-center">
            <h2 className="text-xl font-semibold text-gray-900">Your Stories</h2>
            <div className="flex gap-3">
              <Link
                href="/bulk-import"
                className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
              >
                Bulk Import
              </Link>
              <Link
                href="/write"
                className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
              >
                Write New Story
              </Link>
            </div>
          </div>
        </div>

        {/* Story Filters */}
        {!loading && allStories.length > 0 && (
          <StoryFilters
            categories={categories}
            filters={filters}
            onFiltersChange={setFilters}
            showCategoryFilter={true}
            showSortOptions={true}
            showDateFilter={true}
            showSearch={true}
          />
        )}

        {/* Stories List */}
        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <p className="mt-2 text-gray-500">Loading your stories...</p>
          </div>
        ) : error ? (
          <div className="text-center py-12">
            <p className="text-red-600">{error}</p>
          </div>
        ) : filteredAndSortedStories.length === 0 ? (
          <div className="text-center py-12">
            <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <h3 className="mt-2 text-sm font-medium text-gray-900">No stories yet</h3>
            <p className="mt-1 text-sm text-gray-500">Get started by writing your first story.</p>
            <div className="mt-6">
              <Link
                href="/write"
                className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              >
                Write Your First Story
              </Link>
            </div>
          </div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {filteredAndSortedStories.map((story) => (
              <article key={story.id} className="bg-white rounded-xl shadow-sm overflow-hidden border border-gray-100 hover:shadow-lg transition-all duration-300">
                {/* Story Header */}
                <div className="h-32 bg-gradient-to-br from-gray-50 to-gray-100 relative">
                  <div className="absolute top-3 left-3 right-3 flex justify-between items-start">
                    <div className="flex flex-wrap gap-2">
                      <span className={getStatusBadge(story.status)}>
                        {story.status}
                      </span>
                      {story.privacy === 'PRIVATE' && (
                        <span className="inline-flex items-center px-2 py-1 text-xs font-medium rounded-full bg-amber-100 text-amber-800">
                          <svg className="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                          </svg>
                          Private
                        </span>
                      )}
                      {/* Category Badge */}
                      {story.category && (
                        <span 
                          className="inline-flex items-center px-2.5 py-1 text-xs font-semibold rounded-full text-white border border-opacity-20 border-white shadow-sm"
                          style={{ 
                            backgroundColor: story.category.color || '#6B7280',
                            filter: 'brightness(0.9) saturate(1.1)'
                          }}
                        >
                          {story.category.name}
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="absolute bottom-3 left-3 right-3">
                    <h3 className="text-lg font-bold text-gray-900 line-clamp-2">
                      {story.title}
                    </h3>
                  </div>
                </div>

                <div className="p-5">
                  {/* Story Excerpt */}
                  <p className="text-gray-600 text-sm leading-relaxed mb-4 line-clamp-3">
                    {story.excerpt || (story.content?.length > 120 
                      ? `${story.content.substring(0, 120)}...` 
                      : story.content) || 'No content available.'}
                  </p>

                  {/* Story Metadata */}
                  <div className="flex items-center justify-between text-xs text-gray-500 mb-4">
                    <span>Created {formatDateShort(story.createdAt)}</span>
                    <div className="flex items-center space-x-3">
                      <span className="flex items-center">
                        <svg className="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                        </svg>
                        {story.viewCount || 0}
                      </span>
                      <span className="flex items-center">
                        <svg className="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                        </svg>
                        {story.likeCount || 0}
                      </span>
                      <span className="flex items-center">
                        <svg className="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                        </svg>
                        {story.commentCount || 0}
                      </span>
                      <span>{story.readTime || 5} min</span>
                    </div>
                  </div>

                  {/* Category Selection */}
                  <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2">Category</label>
                    <div className="relative">
                      <select
                        value={story.category?.id || ''}
                        onChange={(e) => handleCategoryChange(story.id, e.target.value ? parseInt(e.target.value) : null)}
                        className="w-full text-sm border border-gray-300 rounded-md px-3 py-2 pr-10 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 shadow-sm hover:border-gray-400 transition-colors font-medium"
                        style={{
                          color: story.category?.color || '#374151'
                        }}
                      >
                        <option value="" style={{ color: '#6B7280' }}>Select Category</option>
                        {categories.map((category) => (
                          <option key={category.id} value={category.id} style={{ color: category.color }}>
                            {category.name}
                          </option>
                        ))}
                      </select>
                      {story.category && (
                        <div 
                          className="absolute right-3 top-1/2 transform -translate-y-1/2 w-4 h-4 rounded-full border-2 border-white shadow-sm"
                          style={{ backgroundColor: story.category.color }}
                        />
                      )}
                    </div>
                  </div>

                  {/* Action Buttons */}
                  <div className="grid grid-cols-2 gap-2">
                    <Link
                      href={`/write/${story.id}`}
                      className="inline-flex items-center justify-center px-3 py-2 text-sm font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 rounded-lg transition-colors duration-200"
                    >
                      <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                      </svg>
                      Edit
                    </Link>
                    <button
                      onClick={() => handleTogglePublish(story)}
                      className={`inline-flex items-center justify-center px-3 py-2 text-sm font-medium rounded-lg transition-colors duration-200 ${
                        story.status === 'PUBLISHED' 
                          ? 'text-orange-600 bg-orange-50 hover:bg-orange-100' 
                          : 'text-green-600 bg-green-50 hover:bg-green-100'
                      }`}
                    >
                      <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        {story.status === 'PUBLISHED' ? (
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
                        ) : (
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        )}
                      </svg>
                      {story.status === 'PUBLISHED' ? 'Unpublish' : 'Publish'}
                    </button>
                  </div>

                  {/* Secondary Actions */}
                  <div className="mt-3 flex justify-between items-center">
                    <Link
                      href={`/story/${story.id}`}
                      className="text-sm text-gray-500 hover:text-gray-700 transition-colors"
                    >
                      View Story →
                    </Link>
                    <button
                      onClick={() => handleDeleteStory(story.id)}
                      className="text-sm text-red-500 hover:text-red-700 transition-colors"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default DashboardPage;
