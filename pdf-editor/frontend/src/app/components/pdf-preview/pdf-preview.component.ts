import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-pdf-preview',
  templateUrl: './pdf-preview.component.html',
  styleUrls: ['./pdf-preview.component.scss']
})
export class PdfPreviewComponent {
  @Input() previewUrl: string | null = null;
  @Input() pageCount = 0;
  @Input() currentPage = 1;
} 