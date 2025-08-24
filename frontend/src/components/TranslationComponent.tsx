'use client';

import React, { useState } from 'react';
import { translationService } from '@/services/api';

interface TranslationComponentProps {
  text: string;
  onTranslationUpdate?: (translatedText: string, language: string) => void;
  className?: string;
}

const TranslationComponent: React.FC<TranslationComponentProps> = ({
  text,
  onTranslationUpdate,
  className = ''
}) => {
  const [isTranslating, setIsTranslating] = useState(false);
  const [translatedText, setTranslatedText] = useState('');
  const [sourceLanguage, setSourceLanguage] = useState('');
  const [targetLanguage, setTargetLanguage] = useState('en');
  const [showTranslation, setShowTranslation] = useState(false);
  const [error, setError] = useState('');
  const [detecting, setDetecting] = useState(false);

  const languageNames = {
    'te': 'Telugu',
    'en': 'English',
    'hi': 'Hindi',
    'ta': 'Tamil',
    'kn': 'Kannada',
    'ml': 'Malayalam'
  };

  const handleTranslate = async () => {
    if (!text || text.trim().length === 0) {
      setError('No text to translate');
      return;
    }

    setIsTranslating(true);
    setError('');

    try {
      const result = await translationService.translateText(text, undefined, targetLanguage);
      setTranslatedText(result.translatedText);
      setSourceLanguage(result.sourceLanguage);
      setShowTranslation(true);
      
      if (onTranslationUpdate) {
        onTranslationUpdate(result.translatedText, result.targetLanguage);
      }
    } catch (err) {
      setError('Translation failed. Please try again.');
      console.error('Translation error:', err);
    } finally {
      setIsTranslating(false);
    }
  };

  const detectLanguage = async () => {
    if (!text || text.trim().length === 0) {
      return;
    }
    
    setDetecting(true);
    
    // Always do client-side detection first for immediate feedback
    let clientSideDetection = 'en';
    if (text.match(/[\u0C00-\u0C7F]/)) {
      clientSideDetection = 'te';
    }
    
    // Set client-side result immediately
    setSourceLanguage(clientSideDetection);
    
    try {
      // Try server-side detection to verify/override
      const result = await translationService.detectLanguage(text);
      
      if (result && result.detectedLanguage) {
        setSourceLanguage(result.detectedLanguage);
      }
    } catch (err) {
      console.error('Server-side language detection failed:', err);
    } finally {
      setDetecting(false);
    }
  };

  React.useEffect(() => {
    // Reset source language when text changes
    setSourceLanguage('');
    setDetecting(false);
    setShowTranslation(false);
    
    if (text && text.length > 0) {
      // Immediate detection without delay
      detectLanguage();
    }
  }, [text]);

  return (
    <div className={`translation-component ${className}`}>
      <div className="flex flex-col sm:flex-row items-start sm:items-center gap-3 mb-4">
        <div className="flex items-center gap-3">
          <select
            value={targetLanguage}
            onChange={(e) => setTargetLanguage(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm font-medium text-gray-900 bg-white"
            disabled={isTranslating}
          >
            <option value="en" className="text-gray-900 bg-white">English</option>
            <option value="te" className="text-gray-900 bg-white">Telugu</option>
            <option value="hi" className="text-gray-900 bg-white">Hindi</option>
            <option value="ta" className="text-gray-900 bg-white">Tamil</option>
            <option value="kn" className="text-gray-900 bg-white">Kannada</option>
            <option value="ml" className="text-gray-900 bg-white">Malayalam</option>
          </select>
          
          <button
            onClick={handleTranslate}
            disabled={isTranslating || !text}
            className="px-4 py-2 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-sm flex items-center gap-2 shadow-sm"
          >
            {isTranslating ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                Translating...
              </>
            ) : (
              <>
                🌍 Translate
              </>
            )}
          </button>
        </div>
      </div>

      <div className="flex flex-wrap items-center gap-3 mb-4">
        {sourceLanguage && !detecting && (
          <div className="text-sm font-medium text-gray-900 bg-white px-3 py-2 rounded-lg border border-gray-300 shadow-sm">
            Detected: <span className="font-bold text-blue-700">{languageNames[sourceLanguage as keyof typeof languageNames] || sourceLanguage}</span>
          </div>
        )}
        
        {detecting && (
          <div className="text-sm font-medium text-blue-700 bg-blue-100 px-3 py-2 rounded-lg border border-blue-300">
            <div className="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-blue-700 mr-2"></div>
            Detecting language...
          </div>
        )}
      </div>

      {error && (
        <div className="text-red-700 text-sm mb-3 p-3 bg-red-50 border border-red-200 rounded-md flex items-center gap-2">
          <span className="text-red-500">⚠️</span>
          <span>{error}</span>
        </div>
      )}

      {showTranslation && translatedText && (
        <div className="mt-3 p-4 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg border border-blue-200 shadow-sm">
          <div className="flex justify-between items-center mb-3">
            <h4 className="font-semibold text-blue-900 flex items-center gap-2">
              <span className="text-lg">🔄</span>
              Translation ({languageNames[sourceLanguage as keyof typeof languageNames]} → {languageNames[targetLanguage as keyof typeof languageNames]})
            </h4>
            <button
              onClick={() => setShowTranslation(false)}
              className="text-blue-600 hover:text-blue-800 text-sm px-2 py-1 rounded-md hover:bg-blue-100 transition-colors"
            >
              ✕ Hide
            </button>
          </div>
          <div className="text-gray-900 whitespace-pre-wrap leading-relaxed bg-white p-3 rounded-md border border-blue-100">
            {translatedText}
          </div>
          <div className="text-xs text-blue-700 mt-3 bg-blue-100 p-2 rounded-md">
            💡 <strong>Demo Translation:</strong> In production, integrate with Google Translate or similar service for accurate translations.
          </div>
        </div>
      )}
    </div>
  );
};

export default TranslationComponent;
