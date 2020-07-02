import {Observable, of} from 'rxjs';
import {IUser} from '../../interfaces/IUser';
import {urls} from '../../environments/environment';

export default class ManagementServiceStub {

  users= [{
    'lastName': 'admin', 'firstName': 'admin', 'mail': 'admin', 'password': '',
    'institution': 'UGent', barcode: '0000000006002', 'augentID': '0000000006002', 'penaltyPoints': -1, 'birthDate': {'year': 1999, 'month': 12, 'day': 27, 'hrs': 0, 'min': 0, 'sec': 0}, 'roles': ['EMPLOYEE', 'ADMIN']
  } as IUser];
  getUserByEmail(email: string): Observable<IUser[]> {
   return of(this.users);
  }

  getUsersByFirstName(firstName: string): Observable<IUser[]> {
    return of(this.users);
  }

  getUsersByName(name: string): Observable<IUser[]> {
    return of(this.users);
  }

  getUserByAugentID(augentID: string): Observable<IUser[]> {
    return of(this.users);
  }

  /** PUT: update the user on the server */
  updateUser(user: IUser): Observable<IUser> {
    return of(this.users[0]);
  }

  /** POST: add the user to the server */
  addUser(user: IUser): Observable<IUser> {
    return of(this.users[0]);
  }

  async existsUser(email: string): Promise<boolean> {
    return of(email === "exists").toPromise();
  }

  addUserByEmployee(user: IUser): Observable<IUser> {
    return of(null);
  }

  getUsersNamesByRole(role: string): Observable<string[]>{
    return of([]);
  }

  updateOwnProfile(){

  }
}
