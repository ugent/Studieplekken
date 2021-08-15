import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { User } from '../model/User';

@Component({
  selector: 'app-search-user-component',
  templateUrl: './search-user-component.component.html',
  styleUrls: ['./search-user-component.component.scss']
})
export class SearchUserComponentComponent implements OnInit {

  @Input() icon: string = "icon-hamburger";
  @Output() selectedUser = new EventEmitter<User>();
  users: User[];


  constructor() { }

  ngOnInit(): void {
  }

  newUsers(users: User[]) {
    this.users = users;
  }

  outputUser(user: User) {
    this.selectedUser.next(user);
  }

}
