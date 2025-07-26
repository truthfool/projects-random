# AI-Powered Log Analyzer

A powerful log analysis tool that uses generative AI to analyze logs from different directories, provide deep insights, and recommend solutions based on system context.

## Features

### 🔍 Core Analysis Features

- **Intelligent Log Parsing**: Parse various log formats with automatic pattern recognition
- **AI-Powered Analysis**: Deep analysis using transformers and vector similarity
- **Pattern Detection**: Identify recurring issues and anomalies
- **Root Cause Analysis**: AI-driven root cause identification
- **Smart Recommendations**: Context-aware solution recommendations

### 🧠 Context Learning

- **System Context**: Learn from system documentation and architecture
- **Error Patterns**: Build knowledge base of known issues and solutions
- **Vector Search**: Semantic search for relevant context
- **Continuous Learning**: Improve analysis with new context

### 📊 Analysis Features

- **Multi-file Analysis**: Analyze logs from multiple sources
- **Temporal Analysis**: Identify time-based patterns
- **Correlation Detection**: Find related events across logs
- **Severity Assessment**: Smart severity scoring
- **Impact Analysis**: Understand issue impact

## Project Structure

```
log-analyzer/
├── src/
│   ├── api/              # FastAPI application
│   ├── models/           # Data models
│   ├── services/         # Core services
│   └── config.py         # Configuration
├── data/                 # Data storage
├── logs/                 # Log file storage
├── context/             # System context storage
├── tests/               # Test suite
└── README.md            # This file
```

## Installation

1. **Clone the Repository**

   ```bash
   git clone <repository-url>
   cd log-analyzer
   ```

2. **Create Virtual Environment**

   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. **Install Dependencies**
   ```bash
   pip install -r requirements.txt
   ```

## Usage

### 1. Start the Server

```bash
uvicorn src.api.main:app --reload
```

### 2. Add System Context

```bash
# Add system documentation
curl -X POST "http://localhost:8000/context/add" \
     -H "Content-Type: application/json" \
     -d '{
       "content": "System architecture document...",
       "doc_type": "architecture",
       "title": "System Overview",
       "tags": ["architecture", "overview"]
     }'

# Add known error solution
curl -X POST "http://localhost:8000/context/error-solution" \
     -H "Content-Type: application/json" \
     -d '{
       "error_pattern": "connection timeout",
       "solution": "Check network configuration...",
       "examples": ["Error: Connection timeout"]
     }'
```

### 3. Analyze Logs

```bash
# Upload and analyze logs
curl -X POST "http://localhost:8000/analyze/files" \
     -F "files=@/path/to/log1.log" \
     -F "files=@/path/to/log2.log" \
     -F "context_query=database issues"

# Check analysis status
curl "http://localhost:8000/analyze/status/{task_id}"
```

## API Endpoints

### Analysis Endpoints

- `POST /analyze/files` - Upload and analyze log files
- `GET /analyze/status/{task_id}` - Get analysis status and results

### Context Management

- `POST /context/add` - Add system documentation
- `POST /context/error-solution` - Add known error solution
- `GET /context/search` - Search context documents

## Configuration

Key settings in `config.py`:

```python
# Analysis settings
MAX_LOG_SIZE = 100 * 1024 * 1024  # 100MB
MAX_CONTEXT_DOCS = 5
SIMILARITY_THRESHOLD = 0.75

# Vector DB settings
EMBEDDING_MODEL = "sentence-transformers/all-mpnet-base-v2"
CHUNK_SIZE = 1000
CHUNK_OVERLAP = 200
```

## Adding System Context

### Documentation Types

1. **Architecture Documents**

   ```python
   content = """
   System Architecture Overview
   - Component A: Handles user authentication
   - Component B: Manages data processing
   """
   doc_type = "architecture"
   title = "System Architecture"
   tags = ["architecture", "components"]
   ```

2. **Error Solutions**

   ```python
   error_pattern = "connection timeout"
   solution = """
   Common causes:
   1. Network congestion
   2. Service unavailable
   3. Firewall blocking

   Solutions:
   1. Check network status
   2. Verify service health
   3. Review firewall rules
   """
   examples = [
       "Error: Database connection timeout",
       "Failed to connect: timeout"
   ]
   ```

## Analysis Output

The analysis provides:

1. **Pattern Analysis**

   - Recurring issues
   - Temporal patterns
   - Anomaly detection

2. **Root Cause Analysis**

   - Primary cause identification
   - Contributing factors
   - Impact assessment

3. **Recommendations**
   - Solution suggestions
   - Prevention measures
   - Best practices

Example output:

```json
{
  "patterns": [
    {
      "pattern": "connection_timeout",
      "occurrences": 15,
      "severity": "high",
      "temporal_distribution": "clustered"
    }
  ],
  "root_causes": [
    {
      "issue": "Database timeouts",
      "cause": "Network congestion",
      "confidence": 0.85
    }
  ],
  "recommendations": [
    {
      "action": "Increase connection timeout",
      "priority": "high",
      "implementation": "Update database configuration"
    }
  ]
}
```

## Development

### Adding New Features

1. **New Log Parser**

   ```python
   class CustomLogParser:
       def parse_file(self, file_path: Path) -> List[LogEntry]:
           # Implementation
   ```

2. **New Analysis Component**
   ```python
   class CustomAnalyzer:
       def analyze(self, entries: List[LogEntry]) -> Analysis:
           # Implementation
   ```

### Running Tests

```bash
pytest tests/
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
