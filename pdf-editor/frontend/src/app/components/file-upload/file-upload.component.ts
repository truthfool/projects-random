import { Component, EventEmitter, Output } from '@angular/core';
import { PdfService, FileInfo } from '../../services/pdf.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent {
  @Output() fileUploaded = new EventEmitter<FileInfo>();
  
  isDragOver = false;
  isUploading = false;
  allowedTypes = '.pdf,.docx,.doc';

  constructor(
    private pdfService: PdfService,
    private snackBar: MatSnackBar
  ) { }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.uploadFile(files[0]);
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.uploadFile(input.files[0]);
    }
  }

  private uploadFile(file: File): void {
    if (!this.isValidFile(file)) {
      this.snackBar.open('Invalid file type. Please upload PDF or Word documents.', 'Close', {
        duration: 3000
      });
      return;
    }

    this.isUploading = true;
    this.pdfService.uploadFile(file).subscribe({
      next: (fileInfo: FileInfo) => {
        this.isUploading = false;
        this.fileUploaded.emit(fileInfo);
        this.snackBar.open('File uploaded successfully!', 'Close', {
          duration: 3000
        });
      },
      error: (error) => {
        this.isUploading = false;
        this.snackBar.open(`Upload failed: ${error}`, 'Close', {
          duration: 5000
        });
      }
    });
  }

  private isValidFile(file: File): boolean {
    const validTypes = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'application/msword'];
    return validTypes.includes(file.type);
  }
} 