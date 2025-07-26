import { Component, OnInit } from '@angular/core';
import { PdfService, FileInfo, EditOperation } from '../../services/pdf.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-pdf-editor',
  templateUrl: './pdf-editor.component.html',
  styleUrls: ['./pdf-editor.component.scss']
})
export class PdfEditorComponent implements OnInit {
  selectedFile: FileInfo | null = null;
  pdfPreview: string | null = null;
  pageCount = 0;
  currentPage = 1;
  loading = false;
  
  // Edit tools
  activeTool: 'text' | 'image' | 'rectangle' | 'line' | null = null;
  edits: EditOperation[] = [];
  
  // Text edit properties
  textContent = '';
  fontSize = 12;
  textColor = [0, 0, 0];
  
  // Shape properties
  shapeWidth = 100;
  shapeHeight = 100;
  shapeColor = [0, 0, 0];
  lineWidth = 1;

  constructor(
    private pdfService: PdfService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    // Load files and listen for file selection
  }

  onFileSelect(file: FileInfo): void {
    if (file.type.toLowerCase() === 'pdf') {
      this.selectedFile = file;
      this.loadPdfPreview();
    } else {
      this.snackBar.open('Please select a PDF file for editing', 'Close', {
        duration: 3000
      });
    }
  }

  loadPdfPreview(): void {
    if (!this.selectedFile) return;
    
    this.loading = true;
    this.pdfService.getPdfPreview(this.selectedFile.filename).subscribe({
      next: (data) => {
        this.pdfPreview = data.preview;
        this.pageCount = data.page_count;
        this.currentPage = 1;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open(`Failed to load PDF preview: ${error}`, 'Close', {
          duration: 5000
        });
      }
    });
  }

  setActiveTool(tool: 'text' | 'image' | 'rectangle' | 'line' | null): void {
    this.activeTool = tool;
  }

  onCanvasClick(event: MouseEvent): void {
    if (!this.activeTool || !this.selectedFile) return;
    
    const canvas = event.target as HTMLCanvasElement;
    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    
    const edit: EditOperation = {
      type: this.activeTool,
      page: this.currentPage,
      x: x,
      y: y
    };
    
    switch (this.activeTool) {
      case 'text':
        if (this.textContent.trim()) {
          edit.text = this.textContent;
          edit.font_size = this.fontSize;
          edit.color = this.textColor;
          this.edits.push(edit);
          this.textContent = '';
        }
        break;
        
      case 'rectangle':
        edit.width = this.shapeWidth;
        edit.height = this.shapeHeight;
        edit.color = this.shapeColor;
        this.edits.push(edit);
        break;
        
      case 'line':
        edit.x1 = x;
        edit.y1 = y;
        edit.x2 = x + 100;
        edit.y2 = y + 100;
        edit.color = this.shapeColor;
        edit.width = this.lineWidth;
        this.edits.push(edit);
        break;
    }
  }

  onImageUpload(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      const reader = new FileReader();
      reader.onload = (e) => {
        const imageData = e.target?.result as string;
        // Add image edit operation
        const edit: EditOperation = {
          type: 'image',
          page: this.currentPage,
          x: 100,
          y: 100,
          image_data: imageData,
          width: this.shapeWidth,
          height: this.shapeHeight
        };
        this.edits.push(edit);
      };
      reader.readAsDataURL(file);
    }
  }

  removeEdit(index: number): void {
    this.edits.splice(index, 1);
  }

  saveEdits(): void {
    if (!this.selectedFile || this.edits.length === 0) {
      this.snackBar.open('No edits to save', 'Close', {
        duration: 3000
      });
      return;
    }
    
    this.loading = true;
    this.pdfService.editPdf(this.selectedFile.filename, this.edits).subscribe({
      next: (result) => {
        this.loading = false;
        this.snackBar.open('PDF edited successfully!', 'Close', {
          duration: 3000
        });
        this.edits = [];
        this.activeTool = null;
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open(`Failed to save edits: ${error}`, 'Close', {
          duration: 5000
        });
      }
    });
  }

  downloadEdited(): void {
    if (!this.selectedFile) return;
    
    this.pdfService.downloadFile(`edited_${this.selectedFile.filename}`, true).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `edited_${this.selectedFile?.original_name}`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      },
      error: (error) => {
        this.snackBar.open(`Download failed: ${error}`, 'Close', {
          duration: 5000
        });
      }
    });
  }

  clearEdits(): void {
    this.edits = [];
    this.activeTool = null;
  }
} 