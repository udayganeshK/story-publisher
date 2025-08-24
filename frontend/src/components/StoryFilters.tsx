'use client';

import React, { useState } from 'react';
import { Category } from '@/types/api';

export interface FilterOptions {
  search: string;
  categoryId: number | null;
  sortBy: 'newest' | 'oldest' | 'mostViewed' | 'mostLiked' | 'title';
  dateRange: 'all' | 'today' | 'week' | 'month' | 'year' | 'custom';
  customStartDate: string;
  customEndDate: string;
}

interface StoryFiltersProps {
  categories: Category[];
  filters: FilterOptions;
  onFiltersChange: (filters: FilterOptions) => void;
  showCategoryFilter?: boolean;
  showSortOptions?: boolean;
  showDateFilter?: boolean;
  showSearch?: boolean;
}

const StoryFilters: React.FC<StoryFiltersProps> = ({
  categories,
  filters,
  onFiltersChange,
  showCategoryFilter = true,
  showSortOptions = true,
  showDateFilter = true,
  showSearch = true,
}) => {
  const [isExpanded, setIsExpanded] = useState(false);

  const handleFilterChange = (key: keyof FilterOptions, value: any) => {
    onFiltersChange({
      ...filters,
      [key]: value,
    });
  };

  const clearFilters = () => {
    onFiltersChange({
      search: '',
      categoryId: null,
      sortBy: 'newest',
      dateRange: 'all',
      customStartDate: '',
      customEndDate: '',
    });
  };

  const hasActiveFilters = 
    filters.search !== '' ||
    filters.categoryId !== null ||
    filters.sortBy !== 'newest' ||
    filters.dateRange !== 'all';

  return (
    <div className="bg-white rounded-lg shadow-md border border-gray-200 p-6 mb-6">
      {/* Main Filter Bar */}
      <div className="flex flex-wrap items-end gap-4 mb-4">
        {/* Search */}
        {showSearch && (
          <div className="flex-1 min-w-64">
            <label className="block text-xs font-medium text-gray-700 mb-1">Search Stories</label>
            <div className="relative">
              <svg 
                className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" 
                fill="none" 
                stroke="currentColor" 
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input
                type="text"
                placeholder="Search by title, content, author..."
                value={filters.search}
                onChange={(e) => handleFilterChange('search', e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white text-gray-900 font-medium shadow-sm"
              />
            </div>
          </div>
        )}

        {/* Sort By */}
        {showSortOptions && (
          <div className="min-w-48">
            <label className="block text-xs font-medium text-gray-700 mb-1">Sort By</label>
            <select
              value={filters.sortBy}
              onChange={(e) => handleFilterChange('sortBy', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white text-gray-900 font-medium shadow-sm"
            >
              <option value="newest">Newest First</option>
              <option value="oldest">Oldest First</option>
              <option value="mostViewed">Most Viewed</option>
              <option value="mostLiked">Most Liked</option>
              <option value="title">Title A-Z</option>
            </select>
          </div>
        )}

        {/* Toggle Advanced Filters */}
        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className="flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
        >
          <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 100 4m0-4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 100 4m0-4v2m0-6V4" />
          </svg>
          Filters
          <svg 
            className={`w-4 h-4 ml-2 transition-transform ${isExpanded ? 'rotate-180' : ''}`} 
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </button>

        {/* Clear Filters */}
        {hasActiveFilters && (
          <button
            onClick={clearFilters}
            className="px-4 py-2 text-sm font-medium text-red-600 hover:text-red-700 transition-colors"
          >
            Clear All
          </button>
        )}
      </div>

      {/* Advanced Filters */}
      {isExpanded && (
        <div className="border-t border-gray-200 pt-4 space-y-4">
          {/* Category Filter */}
          {showCategoryFilter && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Category
              </label>
              <div className="flex flex-wrap gap-2">
                <button
                  onClick={() => handleFilterChange('categoryId', null)}
                  className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
                    filters.categoryId === null
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  All Categories
                </button>
                {categories.map((category) => (
                  <button
                    key={category.id}
                    onClick={() => handleFilterChange('categoryId', category.id)}
                    className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
                      filters.categoryId === category.id
                        ? 'text-white'
                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                    style={{
                      backgroundColor: filters.categoryId === category.id ? category.color : undefined,
                    }}
                  >
                    {category.name}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Date Range Filter */}
          {showDateFilter && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Date Range
              </label>
              <div className="flex flex-wrap gap-2 mb-3">
                {[
                  { value: 'all', label: 'All Time' },
                  { value: 'today', label: 'Today' },
                  { value: 'week', label: 'This Week' },
                  { value: 'month', label: 'This Month' },
                  { value: 'year', label: 'This Year' },
                  { value: 'custom', label: 'Custom Range' },
                ].map((option) => (
                  <button
                    key={option.value}
                    onClick={() => handleFilterChange('dateRange', option.value)}
                    className={`px-3 py-1 rounded-lg text-sm font-medium transition-colors ${
                      filters.dateRange === option.value
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                  >
                    {option.label}
                  </button>
                ))}
              </div>

              {/* Custom Date Range */}
              {filters.dateRange === 'custom' && (
                <div className="flex gap-4">
                  <div>
                    <label className="block text-xs font-medium text-gray-600 mb-1">From</label>
                    <input
                      type="date"
                      value={filters.customStartDate}
                      onChange={(e) => handleFilterChange('customStartDate', e.target.value)}
                      className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white text-gray-900 font-medium shadow-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-600 mb-1">To</label>
                    <input
                      type="date"
                      value={filters.customEndDate}
                      onChange={(e) => handleFilterChange('customEndDate', e.target.value)}
                      className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white text-gray-900 font-medium shadow-sm"
                    />
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Active Filters Summary */}
      {hasActiveFilters && (
        <div className="border-t border-gray-200 pt-4 mt-4">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-sm font-medium text-gray-700">Active filters:</span>
            
            {filters.search && (
              <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                Search: "{filters.search}"
                <button
                  onClick={() => handleFilterChange('search', '')}
                  className="ml-1 text-blue-600 hover:text-blue-800"
                >
                  ×
                </button>
              </span>
            )}

            {filters.categoryId && (
              <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                Category: {categories.find(c => c.id === filters.categoryId)?.name}
                <button
                  onClick={() => handleFilterChange('categoryId', null)}
                  className="ml-1 text-purple-600 hover:text-purple-800"
                >
                  ×
                </button>
              </span>
            )}

            {filters.sortBy !== 'newest' && (
              <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                Sort: {filters.sortBy}
                <button
                  onClick={() => handleFilterChange('sortBy', 'newest')}
                  className="ml-1 text-green-600 hover:text-green-800"
                >
                  ×
                </button>
              </span>
            )}

            {filters.dateRange !== 'all' && (
              <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
                Date: {filters.dateRange === 'custom' ? 'Custom Range' : filters.dateRange}
                <button
                  onClick={() => handleFilterChange('dateRange', 'all')}
                  className="ml-1 text-orange-600 hover:text-orange-800"
                >
                  ×
                </button>
              </span>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default StoryFilters;
