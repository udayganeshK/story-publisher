'use client';

import React, { useState, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/AuthContext';

interface ImportJob {
  id: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  totalDocuments: number;
  progress: number;
  processedFiles: number;
  createdStories: number;
  failedFiles: number;
  isCompleted: boolean;
  errors: string[];
}

export default function BulkImportPage() {
  const { user, getToken } = useAuth();
  const router = useRouter();
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  const [dragActive, setDragActive] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [importJob, setImportJob] = useState<ImportJob | null>(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [error, setError] = useState('');

  // Redirect if not authenticated
  if (!user) {
    router.push('/login');
    return null;
  }

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      handleFile(e.target.files[0]);
    }
  };

  const handleFile = async (file: File) => {
    // Validate file
    if (!file.name.toLowerCase().endsWith('.zip')) {
      setError('Please select a ZIP file containing your documents');
      return;
    }

    if (file.size > 100 * 1024 * 1024) { // 100MB limit
      setError('File size must be less than 100MB');
      return;
    }

    setError('');
    setUploading(true);
    setUploadProgress(0);

    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch('http://localhost:8080/api/bulk-import/upload', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${getToken()}`,
        },
        body: formData,
      });

      const data = await response.json();

      if (data.success) {
        setImportJob(data as ImportJob);
        setUploadProgress(100);
        // Start polling for status updates
        pollImportStatus(data.jobId);
      } else {
        setError(data.error || 'Upload failed');
      }
    } catch (err) {
      setError('Upload failed. Please try again.');
      console.error('Upload error:', err);
    } finally {
      setUploading(false);
    }
  };

  const pollImportStatus = async (importJobId: number) => {
    try {
      const response = await fetch(`http://localhost:8080/api/bulk-import/status/${importJobId}`, {
        headers: {
          'Authorization': `Bearer ${getToken()}`,
        },
      });
      
      const data = await response.json();
      
      if (data.success) {
        // Add computed isCompleted field for UI compatibility
        const isCompleted = data.status === 'COMPLETED' || data.status === 'FAILED' || data.status === 'CANCELLED';
        setImportJob((prev) => ({ 
          ...prev, 
          ...(data as ImportJob), 
          isCompleted 
        } as ImportJob));
        
        // Continue polling if not completed
        if (!isCompleted) {
          setTimeout(() => pollImportStatus(importJobId), 2000);
        }
      }
    } catch (err) {
      console.error('Error polling status:', err);
    }
  };

  const openFileDialog = () => {
    fileInputRef.current?.click();
  };

  const resetUpload = () => {
    setImportJob(null);
    setUploadProgress(0);
    setError('');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-4">
            📚 Bulk Import Stories
          </h1>
          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            Upload a ZIP file containing your documents (.docx, .doc, .pdf, .txt) 
            and we&apos;ll create individual stories for each document.
          </p>
        </div>

        {/* Upload Area */}
        {!importJob && (
          <div className="bg-white rounded-lg shadow-md p-8 mb-8">
            <div
              className={`border-2 border-dashed rounded-lg p-12 text-center transition-colors ${
                dragActive
                  ? 'border-blue-500 bg-blue-50'
                  : 'border-gray-300 hover:border-gray-400'
              }`}
              onDragEnter={handleDrag}
              onDragLeave={handleDrag}
              onDragOver={handleDrag}
              onDrop={handleDrop}
            >
              <div className="max-w-md mx-auto">
                <svg
                  className="mx-auto h-16 w-16 text-gray-400 mb-4"
                  stroke="currentColor"
                  fill="none"
                  viewBox="0 0 48 48"
                >
                  <path
                    d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02"
                    strokeWidth={2}
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
                
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  Upload your ZIP file
                </h3>
                
                <p className="text-sm text-gray-600 mb-4">
                  Drag and drop your ZIP file here, or click to browse
                </p>

                <button
                  onClick={openFileDialog}
                  disabled={uploading}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-md font-medium transition-colors disabled:opacity-50"
                >
                  {uploading ? 'Uploading...' : 'Choose File'}
                </button>

                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".zip"
                  onChange={handleFileSelect}
                  className="hidden"
                />

                <div className="mt-6 text-xs text-gray-500">
                  <p className="mb-2">
                    <strong>Supported formats:</strong> .docx, .doc, .pdf, .txt
                  </p>
                  <p>
                    <strong>Maximum size:</strong> 100MB
                  </p>
                </div>
              </div>
            </div>

            {uploading && (
              <div className="mt-6">
                <div className="bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${uploadProgress}%` }}
                  />
                </div>
                <p className="text-sm text-gray-600 mt-2 text-center">
                  Uploading... {uploadProgress}%
                </p>
              </div>
            )}

            {error && (
              <div className="mt-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
                {error}
              </div>
            )}
          </div>
        )}

        {/* Import Progress */}
        {importJob && (
          <div className="bg-white rounded-lg shadow-md p-8">
            <div className="text-center mb-6">
              <h2 className="text-2xl font-bold text-gray-900 mb-2">
                Import in Progress
              </h2>
              <p className="text-gray-600">
                Processing {importJob.totalDocuments} documents...
              </p>
            </div>

            {/* Progress Bar */}
            <div className="mb-6">
              <div className="flex justify-between text-sm text-gray-600 mb-2">
                <span>Progress</span>
                <span>{Math.round(importJob.progress || 0)}%</span>
              </div>
              <div className="bg-gray-200 rounded-full h-3">
                <div
                  className="bg-green-600 h-3 rounded-full transition-all duration-500"
                  style={{ width: `${importJob.progress || 0}%` }}
                />
              </div>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
              <div className="bg-blue-50 p-4 rounded-lg text-center">
                <div className="text-2xl font-bold text-blue-600">
                  {importJob.processedFiles || 0}
                </div>
                <div className="text-sm text-blue-600">Processed</div>
              </div>
              <div className="bg-green-50 p-4 rounded-lg text-center">
                <div className="text-2xl font-bold text-green-600">
                  {importJob.createdStories || 0}
                </div>
                <div className="text-sm text-green-600">Successful</div>
              </div>
              <div className="bg-red-50 p-4 rounded-lg text-center">
                <div className="text-2xl font-bold text-red-600">
                  {importJob.failedFiles || 0}
                </div>
                <div className="text-sm text-red-600">Failed</div>
              </div>
            </div>

            {/* Status */}
            <div className="text-center">
              <div className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${
                importJob.status === 'COMPLETED' 
                  ? 'bg-green-100 text-green-800'
                  : importJob.status === 'FAILED'
                  ? 'bg-red-100 text-red-800'
                  : 'bg-blue-100 text-blue-800'
              }`}>
                {importJob.status === 'RUNNING' && '⏳ Processing...'}
                {importJob.status === 'COMPLETED' && '✅ Completed!'}
                {importJob.status === 'FAILED' && '❌ Failed'}
                {importJob.status === 'PENDING' && '⏸️ Pending...'}
              </div>
            </div>

            {/* Completion Actions */}
            {importJob.isCompleted && (
              <div className="text-center mt-6 space-x-4">
                <button
                  onClick={() => router.push('/dashboard')}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-md font-medium transition-colors"
                >
                  View My Stories
                </button>
                <button
                  onClick={resetUpload}
                  className="bg-gray-600 hover:bg-gray-700 text-white px-6 py-3 rounded-md font-medium transition-colors"
                >
                  Import More
                </button>
              </div>
            )}

            {/* Errors */}
            {importJob.errors && importJob.errors.length > 0 && (
              <div className="mt-6 bg-yellow-50 border border-yellow-200 rounded-md p-4">
                <h3 className="text-sm font-medium text-yellow-800 mb-2">
                  Some documents couldn&apos;t be processed:
                </h3>
                <ul className="text-xs text-yellow-700 space-y-1">
                  {importJob.errors.slice(0, 5).map((error: string, index: number) => (
                    <li key={index}>• {error}</li>
                  ))}
                  {importJob.errors.length > 5 && (
                    <li>... and {importJob.errors.length - 5} more</li>
                  )}
                </ul>
              </div>
            )}
          </div>
        )}

        {/* Instructions */}
        <div className="bg-white rounded-lg shadow-md p-8 mt-8">
          <h2 className="text-xl font-bold text-gray-900 mb-4">
            📋 How it works
          </h2>
          <div className="space-y-4 text-gray-600">
            <div className="flex items-start space-x-3">
              <span className="flex-shrink-0 w-6 h-6 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center text-sm font-medium">1</span>
              <p>Create a ZIP file containing your documents (.docx, .doc, .pdf, .txt files)</p>
            </div>
            <div className="flex items-start space-x-3">
              <span className="flex-shrink-0 w-6 h-6 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center text-sm font-medium">2</span>
              <p>Upload the ZIP file using the form above</p>
            </div>
            <div className="flex items-start space-x-3">
              <span className="flex-shrink-0 w-6 h-6 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center text-sm font-medium">3</span>
              <p>We&apos;ll extract the content from each document and create draft stories</p>
            </div>
            <div className="flex items-start space-x-3">
              <span className="flex-shrink-0 w-6 h-6 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center text-sm font-medium">4</span>
              <p>Review and edit your imported stories in your dashboard</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
