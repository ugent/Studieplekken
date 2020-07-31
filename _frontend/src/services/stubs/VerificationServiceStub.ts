import {Observable, of} from 'rxjs';
import {urls} from '../../environments/environment';

export default class VerificationServiceStub{

  newUser: {email: string, password: string};
  setNewUser(newUser) {
    this.newUser = newUser;
  }

  getNewUser() {
    return this.newUser;
  }

  verify(code: string): Observable<void> {
    return  of();
  }
}
