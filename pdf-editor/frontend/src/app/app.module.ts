import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatSliderModule } from '@angular/material/slider';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDividerModule } from '@angular/material/divider';

import { AppComponent } from './app.component';
import { FileUploadComponent } from './components/file-upload/file-upload.component';
import { FileListComponent } from './components/file-list/file-list.component';
import { PdfEditorComponent } from './components/pdf-editor/pdf-editor.component';
import { PdfPreviewComponent } from './components/pdf-preview/pdf-preview.component';
import { EditToolsComponent } from './components/edit-tools/edit-tools.component';
import { SplitPdfComponent } from './components/split-pdf/split-pdf.component';
import { ConvertPdfComponent } from './components/convert-pdf/convert-pdf.component';

@NgModule({
  declarations: [
    AppComponent,
    FileUploadComponent,
    FileListComponent,
    PdfEditorComponent,
    PdfPreviewComponent,
    EditToolsComponent,
    SplitPdfComponent,
    ConvertPdfComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    MatToolbarModule,
    MatButtonModule,
    MatCardModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatIconModule,
    MatListModule,
    MatChipsModule,
    MatSliderModule,
    MatCheckboxModule,
    MatTabsModule,
    MatDividerModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { } 