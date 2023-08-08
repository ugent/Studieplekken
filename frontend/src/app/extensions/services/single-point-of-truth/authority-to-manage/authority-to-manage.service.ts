import {Injectable} from '@angular/core';
import {Authority} from '../../../../model/Authority';

@Injectable({
    providedIn: 'root',
})
export class AuthorityToManageService {
    authorityData: Authority;

    get authority(): Authority {
        return this.authorityData;
    }

    set authority(authority: Authority) {
        this.authorityData = authority;
    }
}
