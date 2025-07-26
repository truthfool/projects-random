import { Component, OnInit } from '@angular/core';
import { PdfService, FileInfo } from '../../services/pdf.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-split-pdf',
  templateUrl: './split-pdf.component.html',
  styleUrls: ['./split-pdf.component.scss']
})
export class SplitPdfComponent implements OnInit {
  files: FileInfo[] = [];
  selectedFile: FileInfo | null = null;
  pageRanges: string[] = [''];
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
        this.files = files.filter(f => f.type.toLowerCase() === 'pdf');
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
    this.pageRanges = [''];
  }

  addPageRange(): void {
    this.pageRanges.push('');
  }

  removePageRange(index: number): void {
    if (this.pageRanges.length > 1) {
      this.pageRanges.splice(index, 1);
    }
  }

  splitPdf(): void {
    if (!this.selectedFile) {
      this.snackBar.open('Please select a PDF file', 'Close', {
        duration: 3000
      });
      return;
    }

    const ranges = this.pageRanges.filter(range => range.trim() !== '');
    if (ranges.length === 0) {
      this.snackBar.open('Please specify at least one page range', 'Close', {
        duration: 3000
      });
      return;
    }

    this.loading = true;
    this.pdfService.splitPdf(this.selectedFile.filename, ranges).subscribe({
      next: (result) => {
        this.loading = false;
        this.snackBar.open(`PDF split into ${result.split_files.length} files!`, 'Close', {
          duration: 3000
        });
        // Download all split files
        result.split_files.forEach(filename => {
          this.downloadFile(filename, true);
        });
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open(`Split failed: ${error}`, 'Close', {
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