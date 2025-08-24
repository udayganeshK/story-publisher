#!/usr/bin/env python3
"""
Enhanced bulk import script that extracts DOCX files from ZIP archives
while preserving their original creation/modification dates.
"""

import os
import sys
import re
import zipfile
from datetime import datetime
from pathlib import Path
import psycopg2
from psycopg2.extras import RealDictCursor
from docx import Document
import tempfile
import shutil

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'database': 'storypublisher',
    'user': 'udaykanteti',
    'password': None
}

# ZIP file paths
ZIP_FILES = [
    "/Users/udaykanteti/Workspaces/StoryPublisher/backend/stories/short stories 2024-20250824T143009Z-1-001.zip",
    "/Users/udaykanteti/Workspaces/StoryPublisher/backend/stories/stories mine-20250822T072817Z-1-001.zip"
]

def get_database_connection():
    """Get database connection."""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        return conn
    except Exception as e:
        print(f"Error connecting to database: {e}")
        return None

def extract_text_from_docx(file_path):
    """Extract text content from DOCX file."""
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
    """Categorize story based on content length."""
    length = len(content)
    if length <= 300:
        return 1  # Short Stories
    elif length <= 750:
        return 2  # Medium Stories  
    elif length <= 1500:
        return 3  # Long Stories
    else:
        return 4  # Dramas

def generate_slug(title):
    """Generate URL-safe slug from title."""
    if not title:
        return None
    slug = re.sub(r'[^\w\s-]', '', title.lower())
    slug = re.sub(r'[-\s]+', '-', slug)
    return slug.strip('-')

def calculate_read_time(content):
    """Calculate estimated read time in minutes."""
    word_count = len(content.split())
    return max(1, word_count // 200)

def generate_excerpt(content, max_length=200):
    """Generate excerpt from content."""
    if len(content) <= max_length:
        return content
    return content[:max_length] + "..."

def get_author_id(cursor, username):
    """Get author ID from username."""
    cursor.execute("SELECT id FROM users WHERE username = %s", (username,))
    result = cursor.fetchone()
    if result:
        return result[0]
    else:
        raise Exception(f"Author '{username}' not found")

def story_exists(cursor, title, author_id):
    """Check if story with same title and author already exists."""
    cursor.execute(
        "SELECT id FROM stories WHERE title = %s AND author_id = %s",
        (title, author_id)
    )
    return cursor.fetchone() is not None

def zipinfo_to_datetime(zip_info):
    """Convert ZipInfo date_time to datetime object."""
    try:
        return datetime(*zip_info.date_time)
    except Exception:
        return datetime.now()

def import_stories_from_zip():
    """Import stories directly from ZIP files with preserved dates."""
    print("=== BULK STORY IMPORT FROM ZIP WITH DATE PRESERVATION ===")
    
    # Connect to database
    conn = get_database_connection()
    if not conn:
        print("Could not connect to database")
        return
    
    cursor = conn.cursor()
    
    try:
        # Get author ID
        author_id = get_author_id(cursor, "kvsravi")
        print(f"Found author: kvsravi (ID: {author_id})")
        
        # First, delete existing stories
        cursor.execute("DELETE FROM stories WHERE author_id = %s", (author_id,))
        deleted_count = cursor.rowcount
        print(f"Deleted {deleted_count} existing stories")
        conn.commit()
        
        imported_count = 0
        skipped_count = 0
        error_count = 0
        
        for zip_path in ZIP_FILES:
            if not os.path.exists(zip_path):
                print(f"ZIP file not found: {zip_path}")
                continue
                
            print(f"\nProcessing ZIP file: {os.path.basename(zip_path)}")
            
            with zipfile.ZipFile(zip_path, 'r') as zip_file:
                # Get list of DOCX files in the ZIP
                docx_files = [info for info in zip_file.infolist() 
                             if info.filename.endswith('.docx') and not info.is_dir()]
                
                print(f"Found {len(docx_files)} DOCX files in ZIP")
                
                for zip_info in docx_files:
                    try:
                        # Get original file date from ZIP
                        original_date = zipinfo_to_datetime(zip_info)
                        file_name = os.path.basename(zip_info.filename)
                        
                        print(f"Processing: {file_name} (Original Date: {original_date})")
                        
                        # Extract file to temporary location
                        with tempfile.NamedTemporaryFile(suffix='.docx', delete=False) as temp_file:
                            with zip_file.open(zip_info) as source:
                                shutil.copyfileobj(source, temp_file)
                            temp_path = temp_file.name
                        
                        try:
                            # Extract content
                            content = extract_text_from_docx(temp_path)
                            if not content or len(content.strip()) < 50:
                                print(f"  Skipping file with insufficient content: {file_name}")
                                skipped_count += 1
                                continue
                            
                            # Generate title from filename
                            title = os.path.splitext(file_name)[0]
                            
                            # Check if story already exists
                            if story_exists(cursor, title, author_id):
                                print(f"  Story already exists: {title}")
                                skipped_count += 1
                                continue
                            
                            # Categorize story
                            category_id = categorize_by_length(content)
                            
                            # Generate metadata
                            slug = generate_slug(title)
                            excerpt = generate_excerpt(content)
                            read_time = calculate_read_time(content)
                            
                            # Insert story with original file date
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
                                original_date,            # created_at (original ZIP date!)
                                original_date,            # published_at
                                original_date             # updated_at
                            ))
                            
                            imported_count += 1
                            print(f"  ✓ Imported: {title} (Category: {category_id}, Date: {original_date})")
                            
                        finally:
                            # Clean up temporary file
                            os.unlink(temp_path)
                            
                    except Exception as e:
                        print(f"  ✗ Error processing {file_name}: {e}")
                        error_count += 1
                        continue
        
        # Commit all changes
        conn.commit()
        print(f"\n=== IMPORT COMPLETE ===")
        print(f"Successfully imported: {imported_count} stories")
        print(f"Skipped: {skipped_count} stories")
        print(f"Errors: {error_count} stories")
        
    except Exception as e:
        print(f"Database error: {e}")
        conn.rollback()
    finally:
        cursor.close()
        conn.close()

if __name__ == "__main__":
    import_stories_from_zip()
    print("\nImport process completed!")
