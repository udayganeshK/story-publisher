'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import Navbar from '@/components/Navbar';
import { Story } from '@/types/api';
import { storyService } from '@/services/api';

const HomePage: React.FC = () => {
  const [stories, setStories] = useState<Story[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStories = async () => {
      try {
        const publicStories = await storyService.getAllPublicStories();
        // Ensure we always have an array
        setStories(Array.isArray(publicStories) ? publicStories : []);
      } catch (err) {
        setError('Failed to load stories');
        console.error('Error fetching stories:', err);
        // Set empty array on error
        setStories([]);
      } finally {
        setLoading(false);
      }
    };

    fetchStories();
  }, []);

  const formatDate = (dateInput: string | number[]) => {
    let date: Date;
    
    if (Array.isArray(dateInput)) {
      // Backend returns date as array: [year, month, day, hour, minute, second, nanosecond]
      const [year, month, day, hour = 0, minute = 0, second = 0] = dateInput;
      date = new Date(year, month - 1, day, hour, minute, second); // month is 0-indexed in JS
    } else {
      date = new Date(dateInput);
    }
    
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const generateExcerpt = (story: Story): string => {
    // If excerpt exists, use it
    if (story.excerpt && story.excerpt.trim()) {
      return story.excerpt;
    }
    
    // If no excerpt, generate from content
    if (story.content && story.content.trim()) {
      // Remove HTML tags if any and clean up the text
      const cleanContent = story.content.replace(/<[^>]*>/g, '').trim();
      
      // Take first 120 characters for homepage (shorter than stories page)
      if (cleanContent.length <= 120) {
        return cleanContent;
      }
      
      const truncated = cleanContent.substring(0, 120);
      const lastSpaceIndex = truncated.lastIndexOf(' ');
      
      if (lastSpaceIndex > 80) { // Only break at word boundary if it's reasonable
        return truncated.substring(0, lastSpaceIndex) + '...';
      }
      
      return truncated + '...';
    }
    
    // Fallback if no content
    return 'No preview available.';
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      
      {/* Hero Section */}
      <div className="bg-white">
        <div className="max-w-7xl mx-auto py-16 px-4 sm:py-24 sm:px-6 lg:px-8">
          <div className="text-center">
            <h1 className="text-4xl font-extrabold text-gray-900 sm:text-5xl md:text-6xl">
              Share Your Stories
            </h1>
            <p className="mt-3 max-w-md mx-auto text-base text-gray-500 sm:text-lg md:mt-5 md:text-xl md:max-w-3xl">
              A platform for writers to share their stories and connect with readers from around the world.
            </p>
            <div className="mt-5 max-w-md mx-auto sm:flex sm:justify-center md:mt-8">
              <div className="rounded-md shadow">
                <Link
                  href="/signup"
                  className="w-full flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 md:py-4 md:text-lg md:px-10"
                >
                  Get started
                </Link>
              </div>
              <div className="mt-3 rounded-md shadow sm:mt-0 sm:ml-3">
                <Link
                  href="/stories"
                  className="w-full flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-blue-600 bg-white hover:bg-gray-50 md:py-4 md:text-lg md:px-10"
                >
                  Browse stories
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Featured Stories Section */}
      <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
        <div className="text-center">
          <h2 className="text-3xl font-extrabold text-gray-900 sm:text-4xl">
            Featured Stories
          </h2>
          <p className="mt-3 max-w-2xl mx-auto text-xl text-gray-500 sm:mt-4">
            Discover amazing stories from our community of writers
          </p>
        </div>

        {loading ? (
          <div className="mt-12 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <p className="mt-2 text-gray-500">Loading stories...</p>
          </div>
        ) : error ? (
          <div className="mt-12 text-center">
            <p className="text-red-600">{error}</p>
          </div>
        ) : stories.length === 0 ? (
          <div className="mt-12 text-center">
            <p className="text-gray-500">No stories available yet. Be the first to publish a story!</p>
            <div className="mt-6">
              <Link
                href="/write"
                className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              >
                Write Your First Story
              </Link>
            </div>
          </div>
        ) : (
          <div className="mt-12 grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {stories.slice(0, 6).map((story) => (
              <article key={story.id} className="bg-white rounded-xl shadow-sm overflow-hidden hover:shadow-lg transition-all duration-300 border border-gray-100 hover:border-gray-200">
                {/* Cover Image Placeholder */}
                <div className="h-40 bg-gradient-to-br from-blue-50 to-indigo-100 relative">
                  <div className="absolute inset-0 bg-black bg-opacity-10"></div>
                  <div className="absolute bottom-3 left-3 right-3">
                    <h3 className="text-lg font-bold text-white drop-shadow-lg line-clamp-2">
                      {story.title || 'Untitled Story'}
                    </h3>
                  </div>
                </div>

                <div className="p-5">
                  {/* Story Title Again for Visibility */}
                  <div className="mb-3">
                    <h2 className="text-xl font-bold text-gray-900 mb-2">
                      {story.title || 'Untitled Story'}
                    </h2>
                  </div>

                  {/* Author Info */}
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center space-x-2">
                      <div className="h-8 w-8 rounded-full bg-gradient-to-r from-blue-500 to-purple-600 flex items-center justify-center">
                        <span className="text-white text-xs font-semibold">
                          {(story.author?.firstName?.[0] || story.author?.username?.[0] || 'U').toUpperCase()}
                          {(story.author?.lastName?.[0] || '').toUpperCase()}
                        </span>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-900">
                          {story.author?.firstName && story.author?.lastName 
                            ? `${story.author.firstName} ${story.author.lastName}`
                            : story.author?.username || 'Anonymous'
                          }
                        </p>
                        <p className="text-xs text-gray-500">
                          {story.publishedAt ? formatDate(story.publishedAt) : formatDate(story.createdAt)}
                        </p>
                      </div>
                    </div>
                    <span className="text-xs text-gray-400">
                      {story.readTime || 5} min
                    </span>
                  </div>

                  {/* Story Excerpt */}
                  <p className="text-gray-600 text-sm leading-relaxed mb-4 line-clamp-3">
                    {generateExcerpt(story)}
                  </p>

                  {/* Story Metadata */}
                  <div className="flex items-center justify-between text-xs text-gray-500 mb-4">
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
                    </div>
                  </div>

                  {/* Read More Button */}
                  <Link
                    href={`/story/${story.id}`}
                    className="inline-flex items-center w-full justify-center px-3 py-2 text-sm font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 rounded-lg transition-colors duration-200 group"
                  >
                    Read More
                    <svg className="w-3 h-3 ml-1 group-hover:translate-x-1 transition-transform duration-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8l4 4m0 0l-4 4m4-4H3" />
                    </svg>
                  </Link>
                </div>
              </article>
            ))}
          </div>
        )}

        {stories.length > 6 && (
          <div className="mt-12 text-center">
            <Link
              href="/stories"
              className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-blue-600 bg-blue-100 hover:bg-blue-200"
            >
              View all stories
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default HomePage;
