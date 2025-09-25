import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import {UserService} from '../service/user.service';
import {DialogComponent} from '../../shared/dialog/dialog.component';

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
  imports: [FormsModule, CommonModule, DialogComponent],
  standalone: true,
  providers: [UserService]
})
export class RegisterCAComponent {
  dto: CreateCAUserDto = { email: '', name: '', surname: '', organization: '' };
  showDialog = false;
  dialogMessage = '';
  dialogType: 'info' | 'error' | 'confirm' = 'info';

  constructor(private userService: UserService) {}

  onSubmit(form: NgForm) {
    if (!form.valid) {
      // mark all fields as touched so error messages appear
      Object.values(form.controls).forEach(control => {
        (control as any).control.markAsTouched();
      });
      return;
    }

    this.userService.createCAUser(this.dto).subscribe({
      next: (res) => {
        this.dialogMessage = `CA User created successfully! They will receive an email to set their password.`;
        this.dialogType = 'info';
        this.showDialog = true;
        form.resetForm();
      },
      error: (err) => {
        console.error(err);
        this.dialogMessage = `Error when creating CA user.`;
        this.dialogType = 'error';
        this.showDialog = true;
      }
    });
  }

  onClose() {
    this.showDialog = false;
  }
}
