import { Component, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-edit-tools',
  templateUrl: './edit-tools.component.html',
  styleUrls: ['./edit-tools.component.scss']
})
export class EditToolsComponent {
  @Output() toolSelected = new EventEmitter<string>();
  
  activeTool: string | null = null;
  
  selectTool(tool: string): void {
    this.activeTool = tool;
    this.toolSelected.emit(tool);
  }
} 