import { Component, OnInit } from '@angular/core';
import { PdfService, FileInfo } from '../../services/pdf.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-convert-pdf',
  templateUrl: './convert-pdf.component.html',
  styleUrls: ['./convert-pdf.component.scss']
})
export class ConvertPdfComponent implements OnInit {
  files: FileInfo[] = [];
  selectedFile: FileInfo | null = null;
  loading = false;

  constructor(
    private pdfService: PdfService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.loadFiles();
  }

  loadFiles(): void {
    this.pdfService.getFiles().subscribe({
      next: (files) => {
        this.files = files;
      },
      error: (error) => {
        this.snackBar.open(`Failed to load files: ${error}`, 'Close', {
          duration: 5000
        });
      }
    });
  }

  onFileSelect(file: FileInfo): void {
    this.selectedFile = file;
  }

  convertPdfToWord(): void {
    if (!this.selectedFile || this.selectedFile.type.toLowerCase() !== 'pdf') {
      this.snackBar.open('Please select a PDF file', 'Close', {
        duration: 3000
      });
      return;
    }

    this.loading = true;
    this.pdfService.convertPdfToWord(this.selectedFile.filename).subscribe({
      next: (result) => {
        this.loading = false;
        this.snackBar.open('PDF converted to Word successfully!', 'Close', {
          duration: 3000
        });
        this.downloadFile(result.word_filename, true);
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open(`Conversion failed: ${error}`, 'Close', {
          duration: 5000
        });
      }
    });
  }

  convertWordToPdf(): void {
    if (!this.selectedFile || !['docx', 'doc'].includes(this.selectedFile.type.toLowerCase())) {
      this.snackBar.open('Please select a Word document', 'Close', {
        duration: 3000
      });
      return;
    }

    this.loading = true;
    this.pdfService.convertWordToPdf(this.selectedFile.filename).subscribe({
      next: (result) => {
        this.loading = false;
        this.snackBar.open('Word document converted to PDF successfully!', 'Close', {
          duration: 3000
        });
        this.downloadFile(result.pdf_filename, true);
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open(`Conversion failed: ${error}`, 'Close', {
          duration: 5000
        });
      }
    });
  }

  private downloadFile(filename: string, processed: boolean = false): void {
    this.pdfService.downloadFile(filename, processed).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
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
} 