import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import {UserService} from '../service/user.service';

export interface CreateCAUserDto {
  email: string;
  name: string;
  surname: string;
  organization: string;
}

@Component({
  selector: 'app-register-ca',
  templateUrl: './register-ca.component.html',
  styleUrls: ['../../shared/form.css'],
  imports: [FormsModule, CommonModule],
  standalone: true,
  providers: [UserService]
})
export class RegisterCAComponent {
  dto: CreateCAUserDto = { email: '', name: '', surname: '', organization: '' };

  constructor(private userService: UserService) {}

  onSubmit(form: NgForm) {
    if (!form.valid) {
      alert('All fields are required!');
      return;
    }

    this.userService.createCAUser(this.dto).subscribe({
      next: (res) => {
        alert('CA User created successfully! They will receive an email to set their password.');
        form.resetForm();
      },
      error: (err) => {
        console.error(err);
        alert('Failed to create CA user.');
      }
    });
  }
}
