import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { PdfService, FileInfo } from '../../services/pdf.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-file-list',
  templateUrl: './file-list.component.html',
  styleUrls: ['./file-list.component.scss']
})
export class FileListComponent implements OnInit {
  @Output() fileSelected = new EventEmitter<FileInfo>();
  
  files: FileInfo[] = [];
  loading = false;

  constructor(
    private pdfService: PdfService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.loadFiles();
  }

  loadFiles(): void {
    this.loading = true;
    this.pdfService.getFiles().subscribe({
      next: (files) => {
        this.files = files;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open(`Failed to load files: ${error}`, 'Close', {
          duration: 5000
        });
      }
    });
  }

  onFileSelect(file: FileInfo): void {
    this.fileSelected.emit(file);
  }

  onDownload(file: FileInfo): void {
    this.pdfService.downloadFile(file.filename).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = file.original_name;
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

  onDelete(file: FileInfo): void {
    if (confirm(`Are you sure you want to delete ${file.original_name}?`)) {
      this.pdfService.deleteFile(file.filename).subscribe({
        next: () => {
          this.snackBar.open('File deleted successfully', 'Close', {
            duration: 3000
          });
          this.loadFiles();
        },
        error: (error) => {
          this.snackBar.open(`Delete failed: ${error}`, 'Close', {
            duration: 5000
          });
        }
      });
    }
  }

  getFileIcon(type: string): string {
    switch (type.toLowerCase()) {
      case 'pdf':
        return 'picture_as_pdf';
      case 'docx':
      case 'doc':
        return 'description';
      default:
        return 'insert_drive_file';
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
} 