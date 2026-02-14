import { Component } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ProductAssistantService, AskResponse } from '../../services/product-assistant.service';

@Component({
  selector: 'app-product-assistant',
  templateUrl: './product-assistant.component.html',
  styleUrls: ['./product-assistant.component.scss']
})
export class ProductAssistantComponent {
  question = new FormControl('');
  loading = false;
  response: AskResponse | null = null;

  constructor(private assistant: ProductAssistantService) {}

  ask() {
    const q = this.question.value?.trim();
    if (!q) return;
    this.loading = true;
    this.response = null;
    this.assistant.ask(q).subscribe({
      next: (res) => { this.response = res; this.loading = false; },
      error: () => { this.response = { allowed: false, reason: 'Request failed' }; this.loading = false; }
    });
  }
}


