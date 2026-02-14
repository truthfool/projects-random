import os
from dotenv import load_dotenv
from langchain.chains import RetrievalQA
from langchain_openai import ChatOpenAI
from src.vector_store import get_vector_store

# Load environment variables
load_dotenv()

def main():
    persist_directory = os.getenv("PERSIST_DIRECTORY", "./chroma_db")
    
    # 1. Initialize Vector Store (The Memory)
    vector_store = get_vector_store(persist_directory)
    retriever = vector_store.as_retriever(search_kwargs={"k": 3})
    
    # 2. Initialize LLM (The Brain)
    llm = ChatOpenAI(model_name="gpt-4o", temperature=0)
    
    # 3. Create RAG Chain
    qa_chain = RetrievalQA.from_chain_type(
        llm=llm,
        chain_type="stuff",
        retriever=retriever,
        return_source_documents=True
    )
    
    print("Welcome to Confluence RAG! (Type 'exit' to quit)")
    
    while True:
        query = input("\nAsk a question: ")
        if query.lower() in ["exit", "quit", "q"]:
            break
            
        print("Thinking...")
        try:
            result = qa_chain.invoke({"query": query})
            answer = result["result"]
            sources = result["source_documents"]
            
            print(f"\nAnswer: {answer}")
            print("\nSources:")
            for doc in sources:
                print(f"- {doc.metadata.get('title', 'Untitled')} ({doc.metadata.get('source', 'No URL')})")
                
        except Exception as e:
            print(f"An error occurred: {e}")

if __name__ == "__main__":
    main()
