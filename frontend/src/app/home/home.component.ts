import { Component } from '@angular/core';

@Component({
  selector: 'app-home',
  standalone: true,
  template: `<h1>Welcome Home!</h1><p>You are logged in.</p>`,
  styles: [`h1 { color: green; }`]
})
export class HomeComponent {}
