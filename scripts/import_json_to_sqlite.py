#!/usr/bin/env python3
"""
Import remote data from volumes/data.json into SQLite database.
This script directly inserts the data into SQLite tables.
"""

import json
import sqlite3
import sys
from pathlib import Path

def import_data_to_sqlite(json_file='volumes/data.json', db_file='volumes/sqlite.db'):
    """Import data from JSON file into SQLite database."""
    
    print("=" * 60)
    print("Importing Remote Data to SQLite")
    print("=" * 60)
    print()
    
    # Check if files exist
    if not Path(json_file).exists():
        print(f"Error: JSON file not found: {json_file}")
        return False
    
    if not Path(db_file).exists():
        print(f"Error: Database file not found: {db_file}")
        return False
    
    # Load JSON data
    print(f"Reading data from: {json_file}")
    try:
        with open(json_file, 'r') as f:
            data = json.load(f)
        print(f"Found {len(data)} tables in JSON file")
    except Exception as e:
        print(f"Error reading JSON: {e}")
        return False
    
    print()
    
    # Connect to database
    print(f"Connecting to database: {db_file}")
    try:
        conn = sqlite3.connect(db_file)
        cursor = conn.cursor()
        print(f"Connected successfully")
    except Exception as e:
        print(f"Error connecting to database: {e}")
        return False
    
    print()
    
    # Get existing tables in database
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'")
    existing_tables = {row[0] for row in cursor.fetchall()}
    print(f"Existing tables in database: {len(existing_tables)}")
    
    print()
    print("Importing data...")
    print()
    
    # Import data table by table
    imported_count = 0
    skipped_count = 0
    error_count = 0
    
    for table_name, records in data.items():
        # Skip empty tables
        if not records or len(records) == 0:
            continue
        
        # Check if table exists
        if table_name not in existing_tables:
            print(f"  Skipping {table_name}: table doesn't exist in schema")
            skipped_count += 1
            continue
        
        try:
            # Get column names from first record
            columns = list(records[0].keys())
            
            # Prepare insert statement
            placeholders = ','.join(['?' for _ in columns])
            column_names = ','.join([f'"{col}"' for col in columns])
            insert_sql = f'INSERT OR REPLACE INTO "{table_name}" ({column_names}) VALUES ({placeholders})'
            
            # Insert all records
            for record in records:
                values = [record.get(col) for col in columns]
                cursor.execute(insert_sql, values)
            
            conn.commit()
            print(f"  {table_name}: {len(records)} records imported")
            imported_count += 1
            
        except Exception as e:
            print(f"  {table_name}: Error - {str(e)[:80]}")
            error_count += 1
            conn.rollback()
    
    conn.close()
    
    print()
    print("=" * 60)
    print("Import Summary")
    print("=" * 60)
    print(f"  Imported: {imported_count} tables")
    print(f"  Skipped: {skipped_count} tables (not in schema)")
    print(f"  Errors: {error_count} tables")
    print()
    
    if imported_count > 0:
        print("Import completed successfully!")
        return True
    else:
        print("No data was imported")
        return False

if __name__ == "__main__":
    json_file = sys.argv[1] if len(sys.argv) > 1 else 'volumes/data.json'
    db_file = sys.argv[2] if len(sys.argv) > 2 else 'volumes/sqlite.db'
    
    success = import_data_to_sqlite(json_file, db_file)
    sys.exit(0 if success else 1)