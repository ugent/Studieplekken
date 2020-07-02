import {Component, OnInit} from '@angular/core';
import {transition, trigger, useAnimation} from '@angular/animations';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {rowsAnimation} from '../animations';
import {samePasswordsValidator} from '../shared/validators.directive';
import {institutions, minLengthPwd, roles, urls} from '../../environments/environment';
import {AuthenticationService} from '../../services/authentication.service';
import {IUser} from '../../interfaces/IUser';
import {ISearchPossibility} from '../../interfaces/ISearchPossibility';
import {UserService} from '../../services/user.service';
import {VerificationService} from '../../services/verification.service';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject} from 'rxjs';

@Component({
  selector: 'app-user-overview',
  templateUrl: './user-overview.component.html',
  styleUrls: ['./user-overview.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class UserOverviewComponent implements OnInit {
  Object = Object;

  //parameters for validation
  minLengthPwd = minLengthPwd; //minimum length password

  authenticationService: AuthenticationService;
  numbers: number[];
  lower: number;
  upper: number;
  className = 'active selected';
  linesOnPage = 20;
  results: IUser[];
  searchPossibilities: ISearchPossibility[];
  selectedUser: IUser;
  displaySucces = 'none';
  displayFail = 'none';
  httpCode: number;
  disableAddUser = false;

  roles: string[] = [roles.admin, roles.employee, roles.student];
  extraRole: boolean = false;

  institutions = institutions;
  private allResults: IUser[];

  copy: IUser;

  userForm = new FormGroup({
    mail: new FormControl('', Validators.required),
    pwd: new FormControl('', [Validators.required, Validators.minLength(this.minLengthPwd)]),
    confPwd: new FormControl('', Validators.required),
    role: new FormControl('', Validators.required),
    role2: new FormControl()
  }, {validators: samePasswordsValidator});

  displayPenalties = new BehaviorSubject<string>('none');

  constructor(authenticationService: AuthenticationService, public userService: UserService, public verificationService: VerificationService, private http: HttpClient) {
    this.authenticationService = authenticationService;
    this.results = [];
  }

  ngOnInit(): void {
    this.numbers = Array(Math.ceil(this.results.length / 20)).fill(1).map((x, i) => i + 1);
    this.lower = 0;
    this.upper = 19;
    this.searchPossibilities = [{name: 'name', translation: 'management.name', searchFunction: 'getUsersByName'},
      {name: 'studentid', translation: 'management.studentid', searchFunction: 'getUserByAugentID'},
      {name: 'email', translation: 'management.email', searchFunction: 'getUserByEmail'}];

    this.selectedUser = {
      augentID: '',
      institution: '',
      lastName: '',
      mail: '',
      password: '',
      penaltyPoints: 0,
      roles: [],
      firstName: '',
      barcode: ''
    };
    this.copy = this.selectedUser;
  }

  resultsChanged(results: IUser[]) {
    this.allResults = results;
    this.applyInstitutionFilter();
  }

  newPage(i) {
    this.lower = (i - 1) * 20;
    this.upper = (i - 1) * 20 + 19;

    let scrollToTop = window.setInterval(() => {
      let pos = window.pageYOffset;
      if (pos > document.getElementById('resultsTable').offsetTop - 70) {
        window.scrollTo(0, pos - 20); // how far to scroll on each step
      } else {
        window.clearInterval(scrollToTop);
      }
    }, 16);
  }

  floor(i) {
    return Math.floor(i);
  }

  async addUser(value: any) {
    if (this.userForm.valid) {
      this.disableAddUser = true;
      this.verificationService.setNewUser(value);
      let roles = [value.role];
      if(value.role2 != null){
        roles.push(value.role2);
      }

      let user: IUser = {
        lastName: null,
        firstName: null,
        mail: value.mail,
        password: value.pwd,
        institution: null,
        augentID: null,
        roles: roles,
        penaltyPoints: null,
        barcode: null
      };
      this.userService.addUserByEmployee(user).subscribe(n => {
        this.displaySucces = 'block';
      }, error => {
        this.httpCode = error.status;
        this.displayFail = 'block';
      });
    } else {
      //when a field was left empty and untouched this loop will mark it as touched and this will trigger validation
      Object.keys(this.userForm.controls).forEach(field => {
        const control = this.userForm.get(field);
        control.markAsTouched({onlySelf: true});
      });
    }
  }

  cancel(event) {
    event.preventDefault();
    this.userForm.reset();
  }

  applyInstitutionFilter() {
    let institutions = new Set<String>();

    let boxes = document.getElementsByName('checkboxes');
    for (let i = 0; i < boxes.length; i++) {
      if (boxes[i]['checked']) {
        institutions.add(boxes[i]['value']);
      }
    }
    this.results = this.allResults;
    this.results = this.results.filter(item => {
      return institutions.has(item.institution) || item.institution == null;
    });
    this.numbers = Array(Math.ceil(this.results.length / 20)).fill(1).map((x, i) => i + 1);
  }

  //when admin selects user from search results
  setSelectedUser(user: IUser) {
    this.copy = {...user}; //takes a copy
    this.copy.roles = Object.assign([], user.roles); //deeper copy needed, because this is an array
    this.selectedUser = user;
    if (this.copy.roles.length > 1) {
      this.extraRole = true;
    }
  }

  submit() {
    if (this.copy.augentID.trim().length > 0) {
      if (this.copy.roles.length > 1 && this.extraRole == false) {
        this.copy.roles.pop();
      }
      /*
      delete password is necessary to let the backend know the password is not changing and does not need to be encoded again!
       */
      delete this.copy.password;
      this.userService.updateUser(this.copy).subscribe(r => {
        this.extraRole = false;
        this.selectedUser.augentID = this.copy.augentID;
        this.selectedUser.roles = this.copy.roles;
      });
      document.getElementById('closeEditUserModal').click();
    }

  }

  handleReturnFromPenaltiesOverview(value: boolean): void {
    this.displayPenalties.next('none');
    if (value) {
      let augentID = this.selectedUser.augentID;
      this.userService.getUserByAugentID(augentID).subscribe(n => {
        // find the index of the selected user in this.results to change that entry
        // user by augentID will only return 1 value (and will exist because the augentID is from the selected user) -> n[0]
        let idx = this.allResults.findIndex(u => u.augentID === n.augentID);
        if (idx > -1) {
          this.allResults[idx] = n;
          this.applyInstitutionFilter();
          this.setSelectedUser(n);
        }
      });
    }
  }
}
