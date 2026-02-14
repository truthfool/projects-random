import os
from langchain_openai import OpenAIEmbeddings
from langchain_chroma import Chroma

def get_vector_store(persist_directory="./chroma_db"):
    """
    Returns the Chroma vector store. 
    If it exists, it loads it. If not, it creates a new one in memory (until docs are added).
    """
    embeddings = OpenAIEmbeddings()
    vector_store = Chroma(
        persist_directory=persist_directory,
        embedding_function=embeddings
    )
    return vector_store

def add_documents_to_store(chunks, persist_directory="./chroma_db"):
    """
    Adds document chunks to the vector store.
    """
    vector_store = get_vector_store(persist_directory)
    vector_store.add_documents(chunks)
    # Chroma handles persistence automatically in newer versions usually, 
    # but we ensure directory is set.
    print(f"Added {len(chunks)} chunks to vector store at {persist_directory}")
    return vector_store
