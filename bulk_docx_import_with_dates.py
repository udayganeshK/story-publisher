#!/usr/bin/env python3
"""
Bulk import DOCX stories with proper date preservation.
This script reads DOCX files, extracts their metadata and content,
and imports them into the database with preserved creation dates.
"""

import os
import sys
import re
import zipfile
from datetime import datetime
from pathlib import Path
import psycopg2
from psycopg2.extras import RealDictCursor
import xml.etree.ElementTree as ET
from docx import Document
from docx.opc.coreprops import CoreProperties
import unicodedata
import logging

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'storypublisher',
    'user': 'udaykanteti',
    'password': ''  # No password needed for local connection
}

def get_file_modification_date(file_path):
    """Get the modification date of a file"""
    try:
        timestamp = os.path.getmtime(file_path)
        return datetime.fromtimestamp(timestamp)
    except Exception as e:
        print(f"Warning: Could not get modification date for {file_path}: {e}")
        return datetime.now()

def extract_text_from_docx(file_path):
    """Extract text content from a DOCX file"""
    try:
        doc = Document(file_path)
        text = []
        for paragraph in doc.paragraphs:
            if paragraph.text.strip():
                text.append(paragraph.text.strip())
        return '\n\n'.join(text)
    except Exception as e:
        print(f"Error reading DOCX file {file_path}: {e}")
        return None

def categorize_by_length(content):
    """Categorize story based on word count"""
    word_count = len(content.split())
    if word_count <= 100:
        return 1  # Short Stories
    elif word_count <= 300:
        return 2  # Medium Stories  
    elif word_count <= 800:
        return 3  # Long Stories
    else:
        return 4  # Dramas

def generate_slug(title):
    """Generate URL-safe slug from title"""
    if not title:
        return None
    # Convert to lowercase and replace non-alphanumeric chars with hyphens
    slug = re.sub(r'[^\w\s-]', '', title.lower())
    slug = re.sub(r'[-\s]+', '-', slug)
    return slug.strip('-')

def calculate_read_time(content):
    """Calculate estimated read time in minutes (200 words per minute)"""
    word_count = len(content.split())
    return max(1, word_count // 200)

def generate_excerpt(content, max_length=200):
    """Generate excerpt from content"""
    if len(content) <= max_length:
        return content
    return content[:max_length] + "..."

def get_author_id(cursor, username):
    """Get author ID from username"""
    cursor.execute("SELECT id FROM users WHERE username = %s", (username,))
    result = cursor.fetchone()
    if result:
        return result[0]
    else:
        raise Exception(f"Author '{username}' not found")

def story_exists(cursor, title, author_id):
    """Check if story with same title and author already exists"""
    cursor.execute(
        "SELECT id FROM stories WHERE title = %s AND author_id = %s",
        (title, author_id)
    )
    return cursor.fetchone() is not None

def import_stories_with_dates():
    """Main function to import stories with preserved file dates"""
    stories_dir = "/Users/udaykanteti/Downloads/stories mine"
    author_username = "kvsravi"
    
    if not os.path.exists(stories_dir):
        print(f"Stories directory not found: {stories_dir}")
        return
    
    # Connect to database
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # Get author ID
        author_id = get_author_id(cursor, author_username)
        print(f"Found author: {author_username} (ID: {author_id})")
        
        # Note: NOT deleting existing stories - adding to existing collection
        print("Adding new stories to existing collection (no deletion)...")
        
        # Process all DOCX files
        docx_files = list(Path(stories_dir).glob("*.docx"))
        print(f"Found {len(docx_files)} DOCX files to process")
        
        imported_count = 0
        skipped_count = 0
        error_count = 0
        
        for file_path in docx_files:
            try:
                # Get file modification date
                file_date = get_file_modification_date(file_path)
                print(f"Processing: {file_path.name} (Date: {file_date})")
                
                # Extract content
                content = extract_text_from_docx(file_path)
                if not content or not content.strip():
                    print(f"  Skipping empty file: {file_path.name}")
                    skipped_count += 1
                    continue
                
                # Generate title from filename
                title = file_path.stem  # Remove .docx extension
                
                # Check if story already exists
                if story_exists(cursor, title, author_id):
                    print(f"  Story already exists: {title}")
                    skipped_count += 1
                    continue
                
                # Categorize story
                category_id = categorize_by_length(content)
                word_count = len(content.split())
                
                # Generate metadata
                slug = generate_slug(title)
                excerpt = generate_excerpt(content)
                read_time = calculate_read_time(content)
                
                # Insert story with preserved file date
                insert_query = """
                INSERT INTO stories (
                    title, content, excerpt, slug, author_id, category_id,
                    status, privacy, comment_count, like_count, view_count,
                    read_time, created_at, published_at, updated_at
                ) VALUES (
                    %s, %s, %s, %s, %s, %s,
                    %s, %s, %s, %s, %s,
                    %s, %s, %s, %s
                )
                """
                
                cursor.execute(insert_query, (
                    title,                    # title
                    content,                  # content  
                    excerpt,                  # excerpt
                    slug,                     # slug
                    author_id,                # author_id
                    category_id,              # category_id
                    'PUBLISHED',              # status
                    'PUBLIC',                 # privacy
                    0,                        # comment_count
                    0,                        # like_count
                    0,                        # view_count
                    read_time,                # read_time
                    file_date,                # created_at (preserve file date!)
                    file_date,                # published_at (same as created)
                    file_date                 # updated_at (same as created)
                ))
                
                imported_count += 1
                print(f"  ✓ Imported: {title} (Category: {category_id}, Words: {word_count}, Chars: {len(content)}, Date: {file_date})")
                
            except Exception as e:
                print(f"  ✗ Error processing {file_path.name}: {e}")
                error_count += 1
                continue
        
        # Commit all changes
        conn.commit()
        print(f"\n=== IMPORT COMPLETE ===")
        print(f"Successfully imported: {imported_count} stories")
        print(f"Skipped: {skipped_count} stories")
        print(f"Errors: {error_count} stories")
        print(f"Total processed: {len(docx_files)} files")
        
    except Exception as e:
        print(f"Database error: {e}")
        if conn:
            conn.rollback()
    finally:
        if cursor:
            cursor.close()
        if conn:
            conn.close()

if __name__ == "__main__":
    print("=== BULK STORY IMPORT WITH DATE PRESERVATION ===")
    print("This script will import DOCX stories while preserving their file modification dates")
    
    import_stories_with_dates()
    print("\nImport process completed!")
