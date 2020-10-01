import { Injectable } from '@angular/core';
import {Authority} from '../../../shared/model/Authority';

@Injectable({
  providedIn: 'root'
})
export class AuthorityToManageService {
  authorityData: Authority;

  constructor() { }

  get authority(): Authority {
    return this.authorityData;
  }

  set authority(authority: Authority) {
    this.authorityData = authority;
  }
}
