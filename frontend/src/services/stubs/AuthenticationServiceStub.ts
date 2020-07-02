import {IUser} from '../../interfaces/IUser';
import {BehaviorSubject} from 'rxjs';

export default class AuthenticationServiceStub {

  currentUser = new BehaviorSubject<IUser>({
    'lastName': 'admin', 'firstName': 'admin', 'mail': 'admin', 'password': '',
    'institution': 'UGent', barcode: '0000000006002', 'augentID': '0000000006002', 'penaltyPoints': -1, 'roles': ['EMPLOYEE', 'ADMIN']
  });

  getCurrentUser(): IUser {
   /* return {
      'lastName': 'admin', 'firstName': 'admin', 'mail': 'admin', 'password': '',
      'institution': 'UGent', barcode: '0000000006002', 'augentID': '0000000006002', 'penaltyPoints': -1, 'birthDate': {'year': 1999, 'month': 12, 'day': 27, 'hrs': 0, 'min': 0, 'sec': 0}, 'roles': ['EMPLOYEE', 'ADMIN']
    } as IUser;*/
   return this.currentUser.getValue();
  }

  getRequestUser(): IUser{
    return {
      'lastName': 'admin', 'firstName': 'admin', 'mail': 'admin', 'password': '',
      'institution': 'UGent', barcode: '0000000006002', 'augentID': '0000000006002', 'penaltyPoints': -1, 'birthDate': {'year': 1999, 'month': 12, 'day': 27, 'hrs': 0, 'min': 0, 'sec': 0}, 'roles': ['EMPLOYEE', 'ADMIN']
    } as IUser;
  }

  roles = {
    admin: 'ADMIN',
    employee: 'EMPLOYEE',
    student: 'STUDENT'
  };

  login(mail: string, password: string, succes?, fail?) {
    if(mail === "correct" && password==="correct"){
      succes('/dashboard');
    }
    else{
      fail();
    }
  }

  updateUser(u: IUser){

  }

  updateOwnProfile(){

  }

  getCookie(cname): string {
    return "stubCookie"
  }

  currentUserHasRole(...roles : string[]): boolean {
    if(this.currentUser === null || this.currentUser.getValue() === null) return false;
    for(let role of roles){
      if(this.currentUser.getValue().roles.includes(role)){
        return true;
      }
    }
    return false;
  }
}
