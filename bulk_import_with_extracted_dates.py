#!/usr/bin/env python3
"""
Extract ZIP files manually first, then import stories with preserved file modification dates.
This script extracts the ZIP files to get proper file timestamps, then imports to database.
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
import shutil

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'storypublisher',
    'user': 'udaykanteti',
    'password': ''
}

# ZIP file paths and their extraction directories
ZIP_EXTRACTION_CONFIG = [
    {
        'zip_path': "/Users/udaykanteti/Workspaces/StoryPublisher/backend/stories/short stories 2024-20250824T143009Z-1-001.zip",
        'extract_to': "/Users/udaykanteti/Workspaces/StoryPublisher/backend/stories/extracted_with_dates/short_stories_2024"
    },
    {
        'zip_path': "/Users/udaykanteti/Workspaces/StoryPublisher/backend/stories/stories mine-20250822T072817Z-1-001.zip", 
        'extract_to': "/Users/udaykanteti/Workspaces/StoryPublisher/backend/stories/extracted_with_dates/stories_mine"
    }
]

def get_database_connection():
    """Get database connection."""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        return conn
    except Exception as e:
        print(f"Error connecting to database: {e}")
        return None

def extract_zip_files():
    """Extract ZIP files while preserving file timestamps."""
    print("=== EXTRACTING ZIP FILES WITH DATE PRESERVATION ===")
    
    for config in ZIP_EXTRACTION_CONFIG:
        zip_path = config['zip_path']
        extract_to = config['extract_to']
        
        if not os.path.exists(zip_path):
            print(f"ZIP file not found: {zip_path}")
            continue
            
        print(f"\nExtracting: {os.path.basename(zip_path)}")
        print(f"To: {extract_to}")
        
        # Create extraction directory
        os.makedirs(extract_to, exist_ok=True)
        
        # Extract ZIP file
        with zipfile.ZipFile(zip_path, 'r') as zip_file:
            zip_file.extractall(extract_to)
            print(f"Extracted {len(zip_file.namelist())} files")
    
    print("\n=== EXTRACTION COMPLETE ===")

def get_file_modification_date(file_path):
    """Get the modification date of a file."""
    try:
        timestamp = os.path.getmtime(file_path)
        return datetime.fromtimestamp(timestamp)
    except Exception as e:
        print(f"Warning: Could not get modification date for {file_path}: {e}")
        return datetime.now()

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

def find_all_docx_files():
    """Find all DOCX files in extraction directories."""
    docx_files = []
    
    for config in ZIP_EXTRACTION_CONFIG:
        extract_dir = config['extract_to']
        if os.path.exists(extract_dir):
            # Recursively find all DOCX files
            for root, dirs, files in os.walk(extract_dir):
                for file in files:
                    if file.endswith('.docx') and not file.startswith('~$'):
                        full_path = os.path.join(root, file)
                        docx_files.append(full_path)
    
    return docx_files

def import_stories_from_extracted_files():
    """Import stories from extracted DOCX files with preserved file dates."""
    print("\n=== BULK STORY IMPORT WITH PRESERVED FILE DATES ===")
    
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
        
        # Find all DOCX files
        docx_files = find_all_docx_files()
        print(f"Found {len(docx_files)} DOCX files to process")
        
        imported_count = 0
        skipped_count = 0
        error_count = 0
        
        for file_path in docx_files:
            try:
                # Get file modification date (this will be the preserved date!)
                file_date = get_file_modification_date(file_path)
                file_name = os.path.basename(file_path)
                
                print(f"Processing: {file_name} (Date: {file_date})")
                
                # Extract content
                content = extract_text_from_docx(file_path)
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
                    file_date,                # created_at (preserved file date!)
                    file_date,                # published_at
                    file_date                 # updated_at
                ))
                
                imported_count += 1
                print(f"  ✓ Imported: {title} (Category: {category_id}, Date: {file_date})")
                
            except Exception as e:
                print(f"  ✗ Error processing {file_path}: {e}")
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

def import_from_manual_extraction():
    """Import from manually extracted directory with preserved dates."""
    print("=== IMPORTING FROM MANUALLY EXTRACTED DIRECTORY ===")
    manual_dir = "/Users/udaykanteti/Workspaces/StoryPublisher/backend/stories/short stories 2024"
    
    if not os.path.exists(manual_dir):
        print(f"Manual extraction directory not found: {manual_dir}")
        print("Please ensure you have manually extracted the ZIP files to preserve file dates.")
        return
    
    # Check for DOCX files
    docx_files = []
    for root, dirs, files in os.walk(manual_dir):
        for file in files:
            if file.lower().endswith('.docx') and not file.startswith('~$'):
                docx_files.append(os.path.join(root, file))
    
    print(f"Found {len(docx_files)} DOCX files in manually extracted directory")
    
    if not docx_files:
        print("No DOCX files found!")
        return
    
    # Show sample file dates
    print("\nSample file modification dates:")
    for i, file_path in enumerate(docx_files[:5]):
        mod_time = os.path.getmtime(file_path)
        file_date = datetime.fromtimestamp(mod_time)
        filename = os.path.basename(file_path)
        print(f"  {filename}: {file_date.strftime('%Y-%m-%d %H:%M:%S')}")
    
    # Import the stories
    import_stories_from_directory(manual_dir)

def import_stories_from_directory(directory_path):
    """Import stories from a specific directory."""
    print(f"\n=== IMPORTING STORIES FROM: {directory_path} ===")
    
    # Get database connection
    conn = get_database_connection()
    if not conn:
        return
        
    try:
        cursor = conn.cursor()
        
        # Delete existing stories for author 'kvsravi' (user_id = 7)
        print("Deleting existing stories for author 'kvsravi' (user_id = 7)...")
        cursor.execute("DELETE FROM stories WHERE author_id = %s", (7,))
        deleted_count = cursor.rowcount
        print(f"Deleted {deleted_count} existing stories")
        
        # Find all DOCX files
        docx_files = []
        for root, dirs, files in os.walk(directory_path):
            for file in files:
                if file.lower().endswith('.docx') and not file.startswith('~$'):
                    docx_files.append(os.path.join(root, file))
        
        imported_count = 0
        skipped_count = 0
        error_count = 0
        
        for file_path in docx_files:
            try:
                # Get file modification time (this is the preserved original date)
                mod_time = os.path.getmtime(file_path)
                file_date = datetime.fromtimestamp(mod_time)
                
                # Extract title from filename
                title = os.path.splitext(os.path.basename(file_path))[0]
                
                # Extract content from DOCX
                content = extract_text_from_docx(file_path)
                if not content or len(content.strip()) < 100:
                    print(f"  ⚠ Skipping '{title}' - insufficient content")
                    skipped_count += 1
                    continue
                
                # Generate metadata
                slug = generate_slug(title)
                category_id = categorize_by_length(content)
                excerpt = generate_excerpt(content)
                read_time = calculate_read_time(content)
                word_count = len(content.split())
                
                # Insert into database
                cursor.execute("""
                    INSERT INTO stories (
                        title, content, author, category_id, slug, excerpt,
                        read_time, word_count, is_published, is_featured,
                        created_at, updated_at
                    ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                """, (
                    title, content, 'kvsravi', category_id, slug, excerpt,
                    read_time, word_count, True, False,
                    file_date, datetime.now()
                ))
                
                imported_count += 1
                print(f"  ✓ Imported: {title} (Date: {file_date.strftime('%Y-%m-%d')}, Words: {word_count})")
                
            except Exception as e:
                print(f"  ✗ Error processing {file_path}: {e}")
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

def main():
    """Main function."""
    if len(sys.argv) > 1 and sys.argv[1] == "--manual":
        # Use manually extracted directory (recommended for date preservation)
        import_from_manual_extraction()
    elif len(sys.argv) > 1 and sys.argv[1] == "--skip-extract":
        print("Skipping extraction, importing from existing files...")
        import_stories_from_extracted_files()
    else:
        print("Usage:")
        print("  python bulk_import_with_extracted_dates.py --manual    # Use manually extracted files (recommended)")
        print("  python bulk_import_with_extracted_dates.py --skip-extract  # Use programmatically extracted files")
        print("  python bulk_import_with_extracted_dates.py             # Extract and import")
        print("\nFor best date preservation, use --manual option with manually extracted files.")
        
        # Default behavior: try manual first, then fallback
        manual_dir = "/Users/udaykanteti/Workspaces/StoryPublisher/backend/stories/short stories 2024"
        if os.path.exists(manual_dir):
            print(f"\nFound manually extracted directory: {manual_dir}")
            print("Using manual extraction (preserves original file dates)")
            import_from_manual_extraction()
        else:
            print("\nNo manual extraction found. Proceeding with programmatic extraction...")
            # Extract ZIP files first
            extract_zip_files()
            
            # Then import stories
            import_stories_from_extracted_files()

if __name__ == "__main__":
    main()
    print("\nProcess completed!")
