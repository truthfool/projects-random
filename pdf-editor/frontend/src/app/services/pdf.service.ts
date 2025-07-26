import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpEventType } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface FileInfo {
  filename: string;
  original_name: string;
  size: number;
  type: string;
  page_count?: number;
}

export interface EditOperation {
  type: 'text' | 'image' | 'rectangle' | 'line';
  page: number;
  x: number;
  y: number;
  text?: string;
  font_size?: number;
  color?: number[];
  image_data?: string;
  width?: number;
  height?: number;
  x1?: number;
  y1?: number;
  x2?: number;
  y2?: number;
  fill?: number[];
}

@Injectable({
  providedIn: 'root'
})
export class PdfService {
  private apiUrl = 'http://localhost:5000';

  constructor(private http: HttpClient) { }

  uploadFile(file: File): Observable<FileInfo> {
    const formData = new FormData();
    formData.append('file', file);
    
    return this.http.post<FileInfo>(`${this.apiUrl}/upload`, formData)
      .pipe(
        catchError(this.handleError)
      );
  }

  getFiles(): Observable<FileInfo[]> {
    return this.http.get<FileInfo[]>(`${this.apiUrl}/files`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getPdfPreview(filename: string): Observable<{preview: string, page_count: number}> {
    return this.http.get<{preview: string, page_count: number}>(`${this.apiUrl}/preview/${filename}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  editPdf(filename: string, edits: EditOperation[]): Observable<{message: string, edited_filename: string}> {
    return this.http.post<{message: string, edited_filename: string}>(`${this.apiUrl}/edit`, {
      filename,
      edits
    }).pipe(
      catchError(this.handleError)
    );
  }

  convertPdfToWord(filename: string): Observable<{message: string, word_filename: string}> {
    return this.http.post<{message: string, word_filename: string}>(`${this.apiUrl}/convert/pdf-to-word`, {
      filename
    }).pipe(
      catchError(this.handleError)
    );
  }

  convertWordToPdf(filename: string): Observable<{message: string, pdf_filename: string}> {
    return this.http.post<{message: string, pdf_filename: string}>(`${this.apiUrl}/convert/word-to-pdf`, {
      filename
    }).pipe(
      catchError(this.handleError)
    );
  }

  splitPdf(filename: string, ranges: string[]): Observable<{message: string, split_files: string[]}> {
    return this.http.post<{message: string, split_files: string[]}>(`${this.apiUrl}/split`, {
      filename,
      ranges
    }).pipe(
      catchError(this.handleError)
    );
  }

  mergePdfs(filenames: string[]): Observable<{message: string, merged_filename: string}> {
    return this.http.post<{message: string, merged_filename: string}>(`${this.apiUrl}/merge`, {
      filenames
    }).pipe(
      catchError(this.handleError)
    );
  }

  downloadFile(filename: string, processed: boolean = false): Observable<Blob> {
    const url = processed ? 
      `${this.apiUrl}/download/${filename}?processed=true` : 
      `${this.apiUrl}/download/${filename}`;
    
    return this.http.get(url, { responseType: 'blob' })
      .pipe(
        catchError(this.handleError)
      );
  }

  deleteFile(filename: string): Observable<{message: string}> {
    return this.http.delete<{message: string}>(`${this.apiUrl}/delete/${filename}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: any) {
    console.error('An error occurred:', error);
    return throwError(() => error.error?.message || 'Something went wrong');
  }
} 