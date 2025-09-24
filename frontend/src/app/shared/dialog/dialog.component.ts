import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormsModule} from '@angular/forms';

export type DialogType = 'info' | 'error' | 'confirm' | 'download';

@Component({
  selector: 'app-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dialog.component.html',
  styleUrls: ['./dialog.component.css']
})
export class DialogComponent {
  @Input() message: string = '';
  @Input() type: DialogType = 'info';
  @Input() downloadOptions: string[] = [];
  @Output() close = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<{extension?: string, alias?: string, password?: string}>();

  selectedOption: string = '';
  alias: string = '';
  password: string = '';

  onClose() {
    this.close.emit();
  }

  onConfirm() {
    if (this.type === 'download') {
      this.confirm.emit({
        extension: this.selectedOption,
        alias: this.alias,
        password: this.password
      });
    } else {
      this.confirm.emit({});
    }
  }
}

