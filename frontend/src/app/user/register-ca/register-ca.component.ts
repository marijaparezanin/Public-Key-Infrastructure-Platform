import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-register-ca',
  templateUrl: './register-ca.component.html',
  styleUrls: ['../../shared/form.css'],
  imports:[FormsModule,CommonModule],
  standalone:true,
})
export class RegisterCAComponent {
}
