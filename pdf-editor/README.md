# PDF Editor

A comprehensive PDF editing application built with Flask (Python) backend and Angular frontend. This application provides granular PDF editing capabilities, document conversion, and file management features.

## Features

### 🎯 Core Features

- **PDF Upload & Management**: Drag-and-drop file upload with support for PDF, DOCX, and DOC files
- **Granular PDF Editing**: Add text, images, rectangles, and lines to PDF documents
- **Document Conversion**: Convert between PDF and Word formats (PDF ↔ Word)
- **PDF Splitting**: Split PDF documents by page ranges
- **File Management**: Download, delete, and organize uploaded files

### 🛠️ Technical Features

- **Modern UI**: Angular Material design with responsive layout
- **Real-time Preview**: PDF preview with editing capabilities
- **Drag & Drop**: Intuitive file upload interface
- **Error Handling**: Comprehensive error handling and user feedback
- **File Validation**: Support for multiple file formats with validation

## Project Structure

```
pdf-editor/
├── backend/                 # Flask Python backend
│   ├── app.py              # Main Flask application
│   ├── requirements.txt    # Python dependencies
│   ├── uploads/           # Uploaded files storage
│   ├── processed/         # Processed files storage
│   └── temp/              # Temporary files
├── frontend/               # Angular frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/ # Angular components
│   │   │   ├── services/   # API services
│   │   │   └── ...
│   │   ├── styles.scss     # Global styles
│   │   └── main.ts         # Application entry point
│   ├── package.json        # Node.js dependencies
│   └── angular.json        # Angular configuration
└── README.md              # This file
```

## Prerequisites

### Backend Requirements

- Python 3.8 or higher
- pip (Python package manager)

### Frontend Requirements

- Node.js 16 or higher
- npm (Node package manager)
- Angular CLI (`npm install -g @angular/cli`)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd pdf-editor
```

### 2. Backend Setup

```bash
cd backend

# Create virtual environment (recommended)
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run the Flask application
python app.py
```

The backend will be available at `http://localhost:5000`

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start the development server
npm start
```

The frontend will be available at `http://localhost:4200`

## API Endpoints

### File Management

- `POST /upload` - Upload PDF or Word documents
- `GET /files` - List all uploaded files
- `GET /download/<filename>` - Download a file
- `DELETE /delete/<filename>` - Delete a file

### PDF Operations

- `GET /preview/<filename>` - Get PDF preview (first page as image)
- `POST /edit` - Edit PDF with granular details
- `POST /split` - Split PDF into multiple files
- `POST /merge` - Merge multiple PDFs

### Document Conversion

- `POST /convert/pdf-to-word` - Convert PDF to Word document
- `POST /convert/word-to-pdf` - Convert Word document to PDF

## Usage Guide

### 1. Upload Files

- Navigate to the "Upload & Files" tab
- Drag and drop files or click "Choose Files"
- Supported formats: PDF, DOCX, DOC

### 2. Edit PDF Documents

- Select a PDF file from the file list
- Choose an editing tool (Text, Image, Rectangle, Line)
- Configure tool properties
- Click on the PDF preview to add elements
- Save your edits and download the modified PDF

### 3. Convert Documents

- Go to the "Convert" tab
- Select a file to convert
- Choose conversion direction (PDF to Word or Word to PDF)
- Download the converted file

### 4. Split PDF Documents

- Navigate to the "Split PDF" tab
- Select a PDF file
- Configure page ranges (e.g., "1-3", "5", "7-9")
- Split the PDF and download individual files

## Development

### Backend Development

The Flask backend uses:

- **Flask**: Web framework
- **PyMuPDF (fitz)**: PDF manipulation
- **python-docx**: Word document processing
- **reportlab**: PDF generation
- **Pillow**: Image processing

### Frontend Development

The Angular frontend uses:

- **Angular 16**: Frontend framework
- **Angular Material**: UI components
- **RxJS**: Reactive programming
- **TypeScript**: Type-safe JavaScript

### Adding New Features

1. **Backend**: Add new endpoints in `app.py`
2. **Frontend**: Create new components in `src/app/components/`
3. **API Service**: Update `pdf.service.ts` for new endpoints
4. **Testing**: Test both backend and frontend functionality

## Configuration

### Backend Configuration

Edit `backend/app.py` to modify:

- Upload file size limits
- Allowed file types
- Storage directories
- CORS settings

### Frontend Configuration

Edit `frontend/src/app/services/pdf.service.ts` to modify:

- API base URL
- Request/response handling
- Error handling

## Troubleshooting

### Common Issues

1. **Backend won't start**

   - Check Python version (3.8+ required)
   - Verify all dependencies are installed
   - Check if port 5000 is available

2. **Frontend won't start**

   - Check Node.js version (16+ required)
   - Run `npm install` to install dependencies
   - Check if port 4200 is available

3. **File upload fails**

   - Check file size (max 1GB)
   - Verify file format is supported
   - Check backend is running

4. **PDF editing not working**
   - Ensure PyMuPDF is properly installed
   - Check PDF file is not corrupted
   - Verify backend has write permissions

### Logs

- Backend logs are displayed in the terminal
- Frontend logs are available in browser console (F12)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:

- Create an issue in the repository
- Check the troubleshooting section
- Review the API documentation

## Future Enhancements

- [ ] PDF merging functionality
- [ ] OCR text extraction
- [ ] Digital signature support
- [ ] Batch processing
- [ ] Cloud storage integration
- [ ] User authentication
- [ ] Collaborative editing
- [ ] Mobile responsive design improvements
