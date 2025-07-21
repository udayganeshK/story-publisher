'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User } from '@/types/api';
import { authService } from '@/services/api';
import Cookies from 'js-cookie';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (response: any) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const token = Cookies.get('auth_token');
      if (token) {
        try {
          // Fetch user profile from backend using the token
          const response = await fetch('http://localhost:8080/api/auth/profile', {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json',
            },
          });

          if (response.ok) {
            const userData = await response.json();
            setUser(userData);
          } else {
            // Token is invalid, remove it
            console.error('Invalid token, removing from cookies');
            Cookies.remove('auth_token');
            setUser(null);
          }
        } catch (error) {
          console.error('Error fetching user profile:', error);
          Cookies.remove('auth_token');
          setUser(null);
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (response: any) => {
    // Extract token from the response
    const token = response.accessToken;
    Cookies.set('auth_token', token, { expires: 7 }); // 7 days
    
    try {
      // Fetch full user profile from backend
      const profileResponse = await fetch('http://localhost:8080/api/auth/profile', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (profileResponse.ok) {
        const userData = await profileResponse.json();
        setUser(userData);
      } else {
        // Fallback to creating user data from login response
        const userData: User = {
          id: 1, // Placeholder
          username: response.username,
          email: response.email,
          firstName: response.username,
          lastName: '',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        };
        setUser(userData);
      }
    } catch (error) {
      console.error('Error fetching user profile after login:', error);
      // Fallback to creating user data from login response
      const userData: User = {
        id: 1, // Placeholder
        username: response.username,
        email: response.email,
        firstName: response.username,
        lastName: '',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      setUser(userData);
    }
  };

  const logout = () => {
    Cookies.remove('auth_token');
    setUser(null);
    window.location.href = '/';
  };

  const value: AuthContextType = {
    user,
    loading,
    login,
    logout,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
