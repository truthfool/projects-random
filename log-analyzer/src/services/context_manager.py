from pathlib import Path
from typing import Dict, List, Optional
import json
from datetime import datetime
from loguru import logger

from ..config import settings

class ContextManager:
    """Service for managing system context and documentation."""
    
    def __init__(self):
        self.context_dir = settings.CONTEXT_DIR
        self.context_dir.mkdir(parents=True, exist_ok=True)
        
        # Initialize context index
        self.index_file = self.context_dir / "index.json"
        self.context_index = self._load_index()
    
    def add_context(self, content: str, metadata: Dict) -> str:
        """Add new context document."""
        try:
            # Generate unique ID
            context_id = f"{metadata.get('type', 'doc')}_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
            
            # Save content
            file_path = self.context_dir / f"{context_id}.txt"
            file_path.write_text(content)
            
            # Update metadata
            metadata.update({
                "id": context_id,
                "created_at": datetime.now().isoformat(),
                "file_path": str(file_path),
                "size": len(content)
            })
            
            # Update index
            self.context_index[context_id] = metadata
            self._save_index()
            
            logger.info(f"Added new context document: {context_id}")
            return context_id
            
        except Exception as e:
            logger.error(f"Error adding context: {e}")
            raise
    
    def get_context(self, context_id: str) -> Optional[Dict]:
        """Get context document by ID."""
        try:
            if context_id not in self.context_index:
                return None
            
            metadata = self.context_index[context_id]
            file_path = Path(metadata["file_path"])
            
            if not file_path.exists():
                logger.warning(f"Context file not found: {file_path}")
                return None
            
            content = file_path.read_text()
            
            return {
                "content": content,
                "metadata": metadata
            }
            
        except Exception as e:
            logger.error(f"Error getting context {context_id}: {e}")
            return None
    
    def update_context(self, context_id: str, content: Optional[str] = None, 
                      metadata: Optional[Dict] = None) -> bool:
        """Update existing context document."""
        try:
            if context_id not in self.context_index:
                return False
            
            current_metadata = self.context_index[context_id]
            file_path = Path(current_metadata["file_path"])
            
            # Update content if provided
            if content is not None:
                file_path.write_text(content)
                current_metadata["size"] = len(content)
                current_metadata["updated_at"] = datetime.now().isoformat()
            
            # Update metadata if provided
            if metadata:
                current_metadata.update(metadata)
            
            # Save updated index
            self._save_index()
            
            logger.info(f"Updated context document: {context_id}")
            return True
            
        except Exception as e:
            logger.error(f"Error updating context {context_id}: {e}")
            return False
    
    def delete_context(self, context_id: str) -> bool:
        """Delete context document."""
        try:
            if context_id not in self.context_index:
                return False
            
            metadata = self.context_index[context_id]
            file_path = Path(metadata["file_path"])
            
            # Delete file
            if file_path.exists():
                file_path.unlink()
            
            # Remove from index
            del self.context_index[context_id]
            self._save_index()
            
            logger.info(f"Deleted context document: {context_id}")
            return True
            
        except Exception as e:
            logger.error(f"Error deleting context {context_id}: {e}")
            return False
    
    def search_context(self, query: str) -> List[Dict]:
        """Search context documents by metadata."""
        results = []
        
        for context_id, metadata in self.context_index.items():
            # Simple metadata search
            matches = any(
                query.lower() in str(value).lower()
                for value in metadata.values()
            )
            
            if matches:
                context = self.get_context(context_id)
                if context:
                    results.append(context)
        
        return results
    
    def get_all_context(self) -> List[Dict]:
        """Get all context documents."""
        return [
            self.get_context(context_id)
            for context_id in self.context_index
            if self.get_context(context_id) is not None
        ]
    
    def _load_index(self) -> Dict:
        """Load context index from file."""
        if not self.index_file.exists():
            return {}
        
        try:
            return json.loads(self.index_file.read_text())
        except Exception as e:
            logger.error(f"Error loading context index: {e}")
            return {}
    
    def _save_index(self) -> None:
        """Save context index to file."""
        try:
            self.index_file.write_text(json.dumps(self.context_index, indent=2))
        except Exception as e:
            logger.error(f"Error saving context index: {e}")
            raise
    
    def add_system_documentation(self, doc_type: str, content: str, 
                               title: str, tags: List[str] = None) -> str:
        """Add system documentation with standard metadata."""
        metadata = {
            "type": "documentation",
            "doc_type": doc_type,
            "title": title,
            "tags": tags or [],
            "format": "text"
        }
        
        return self.add_context(content, metadata)
    
    def add_error_solution(self, error_pattern: str, solution: str, 
                          examples: List[str] = None) -> str:
        """Add known error solution."""
        metadata = {
            "type": "error_solution",
            "error_pattern": error_pattern,
            "examples": examples or [],
            "format": "text"
        }
        
        return self.add_context(solution, metadata)
    
    def get_error_solutions(self) -> List[Dict]:
        """Get all error solutions."""
        return [
            context for context in self.get_all_context()
            if context["metadata"]["type"] == "error_solution"
        ] 