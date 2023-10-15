import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { User, UserConstructor } from '../../../../model/User';
import { UserService } from '../../api/users/user.service';

/**
 * This service is a helper service used in the UsersManagementComponent
 * and its subcomponent UserDetailsManagementComponent.
 *
 * The idea is the same as the LocationDetailsService.
 */
@Injectable({
  providedIn: 'root',
})
export class UserDetailsService {
  /*
   * 'userSubject' is the BehaviorSubject that keeps track of the
   * user that is viewed in details by the user
   */
  private userSubject: BehaviorSubject<User> = new BehaviorSubject<User>(
    UserConstructor.new()
  );
  /*
   * 'userObs' is the observable that all subcomponents will listen to to get the information to view
   */
  public userObs = this.userSubject.asObservable();

  constructor(private userService: UserService) {}

  loadUser(id: string): void {
    this.userService.getUserByAUGentId(id).subscribe(
      (next) => {
        this.userSubject.next(next);
      },
      (error) => {
        this.userSubject.error(error);
      }
    );
  }
}
