import os
from langchain_community.document_loaders import ConfluenceLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter

def load_confluence_documents(url, username, api_key, space_key=None, limit=50):
    """
    Loads pages from Confluence.
    """
    print(f"Connecting to Confluence at {url}...")
    loader = ConfluenceLoader(
        url=url,
        username=username,
        api_key=api_key,
        cloud=True,  # Assuming cloud for now, can be parameterized
        space_key=space_key,
        limit=limit
    )
    
    documents = loader.load()
    print(f"Loaded {len(documents)} documents from Confluence.")
    return documents

def split_documents(documents, chunk_size=1000, chunk_overlap=200):
    """
    Splits documents into smaller chunks for embedding.
    """
    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=chunk_size,
        chunk_overlap=chunk_overlap,
        separators=["\n\n", "\n", " ", ""]
    )
    chunks = text_splitter.split_documents(documents)
    print(f"Split {len(documents)} documents into {len(chunks)} chunks.")
    return chunks
