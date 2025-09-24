import {inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {CreateCAUserDto} from '../dto/CreateCAUserDto';
import {environment} from '../../../environments/environment';
import {SimpleCertificate} from '../../certificate/model/certificate.model';


@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.serverUrl}/users`;

  createCAUser(dto: CreateCAUserDto): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/create-ca`, dto);
  }


}
