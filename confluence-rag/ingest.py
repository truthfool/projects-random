import os
from dotenv import load_dotenv
from src.ingest_helper import load_confluence_documents, split_documents
from src.vector_store import add_documents_to_store

# Load environment variables
load_dotenv()

def main():
    # Configuration
    url = os.getenv("CONFLUENCE_URL")
    username = os.getenv("CONFLUENCE_USERNAME")
    api_key = os.getenv("CONFLUENCE_API_TOKEN")
    persist_directory = os.getenv("PERSIST_DIRECTORY", "./chroma_db")
    
    # Optional: Filter by Space Key if provided
    # space_key = "DS" # Example
    
    if not all([url, username, api_key]):
        print("Error: Missing Confluence credentials in .env file.")
        return

    # 1. Load Documents
    documents = load_confluence_documents(url=url, username=username, api_key=api_key)
    
    # 2. Split Documents
    chunks = split_documents(documents)
    
    # 3. Store in Vector DB
    add_documents_to_store(chunks, persist_directory=persist_directory)
    
    print("Ingestion complete!")

if __name__ == "__main__":
    main()
