import {Injectable} from '@angular/core';
import {environment} from '../../../../environments/environment';

@Injectable({
    providedIn: 'root',
})
export class EnvironmentService {

    constructor() {
    }

    isProduction(): boolean {
        return environment.production;
    }

    isStaging(): boolean {
        return !this.isProduction() && environment.showStagingWarning;
    }

    isLocal(): boolean {
        return !this.isStaging();
    }
}
