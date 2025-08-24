import { Story } from '@/types/api';
import { FilterOptions } from '@/components/StoryFilters';

// Parse date arrays from backend to Date objects
export const parseDate = (dateInput: string | number[]): Date => {
  if (Array.isArray(dateInput)) {
    // Backend returns date as array: [year, month, day, hour, minute, second, nanosecond]
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateInput;
    return new Date(year, month - 1, day, hour, minute, second); // month is 0-indexed in JS
  } else {
    return new Date(dateInput);
  }
};

// Check if date falls within the specified range
const isDateInRange = (date: Date, range: string, customStart?: string, customEnd?: string): boolean => {
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  
  switch (range) {
    case 'today':
      const storyDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
      return storyDate.getTime() === today.getTime();
    
    case 'week':
      const weekAgo = new Date(today);
      weekAgo.setDate(weekAgo.getDate() - 7);
      return date >= weekAgo;
    
    case 'month':
      const monthAgo = new Date(today);
      monthAgo.setMonth(monthAgo.getMonth() - 1);
      return date >= monthAgo;
    
    case 'year':
      const yearAgo = new Date(today);
      yearAgo.setFullYear(yearAgo.getFullYear() - 1);
      return date >= yearAgo;
    
    case 'custom':
      if (!customStart && !customEnd) return true;
      
      const startDate = customStart ? new Date(customStart) : new Date(0);
      const endDate = customEnd ? new Date(customEnd) : new Date();
      
      // Set end date to end of day
      if (customEnd) {
        endDate.setHours(23, 59, 59, 999);
      }
      
      return date >= startDate && date <= endDate;
    
    case 'all':
    default:
      return true;
  }
};

// Filter stories based on the provided filters
export const filterStories = (stories: Story[], filters: FilterOptions): Story[] => {
  return stories.filter(story => {
    // Search filter
    if (filters.search) {
      const searchTerm = filters.search.toLowerCase();
      const matchesSearch = 
        story.title.toLowerCase().includes(searchTerm) ||
        story.content.toLowerCase().includes(searchTerm) ||
        (story.excerpt && story.excerpt.toLowerCase().includes(searchTerm)) ||
        (story.author?.firstName && story.author.firstName.toLowerCase().includes(searchTerm)) ||
        (story.author?.lastName && story.author.lastName.toLowerCase().includes(searchTerm)) ||
        (story.author?.username && story.author.username.toLowerCase().includes(searchTerm)) ||
        (story.category?.name && story.category.name.toLowerCase().includes(searchTerm));
      
      if (!matchesSearch) return false;
    }

    // Category filter
    if (filters.categoryId !== null) {
      if (!story.category || story.category.id !== filters.categoryId) {
        return false;
      }
    }

    // Date range filter - use createdAt for creation date, publishedAt for published date
    if (filters.dateRange !== 'all') {
      // Use publishedAt if available and story is published, otherwise use createdAt
      const dateToCheck = story.status === 'PUBLISHED' && story.publishedAt 
        ? parseDate(story.publishedAt)
        : parseDate(story.createdAt);
      
      if (!isDateInRange(dateToCheck, filters.dateRange, filters.customStartDate, filters.customEndDate)) {
        return false;
      }
    }

    return true;
  });
};

// Sort stories based on the provided sort option
export const sortStories = (stories: Story[], sortBy: string): Story[] => {
  return [...stories].sort((a, b) => {
    switch (sortBy) {
      case 'newest':
        // Sort by publishedAt if available and published, otherwise by createdAt
        const aDate = a.status === 'PUBLISHED' && a.publishedAt 
          ? parseDate(a.publishedAt)
          : parseDate(a.createdAt);
        const bDate = b.status === 'PUBLISHED' && b.publishedAt 
          ? parseDate(b.publishedAt)
          : parseDate(b.createdAt);
        return bDate.getTime() - aDate.getTime();
      
      case 'oldest':
        const aDateOld = a.status === 'PUBLISHED' && a.publishedAt 
          ? parseDate(a.publishedAt)
          : parseDate(a.createdAt);
        const bDateOld = b.status === 'PUBLISHED' && b.publishedAt 
          ? parseDate(b.publishedAt)
          : parseDate(b.createdAt);
        return aDateOld.getTime() - bDateOld.getTime();
      
      case 'mostViewed':
        return (b.viewCount || 0) - (a.viewCount || 0);
      
      case 'mostLiked':
        return (b.likeCount || 0) - (a.likeCount || 0);
      
      case 'title':
        return a.title.localeCompare(b.title);
      
      default:
        return 0;
    }
  });
};

// Apply both filtering and sorting
export const processStories = (stories: Story[], filters: FilterOptions): Story[] => {
  const filtered = filterStories(stories, filters);
  return sortStories(filtered, filters.sortBy);
};

// Format date for display
export const formatDate = (dateInput: string | number[]): string => {
  const date = parseDate(dateInput);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

// Format date for display (short version)
export const formatDateShort = (dateInput: string | number[]): string => {
  const date = parseDate(dateInput);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};
