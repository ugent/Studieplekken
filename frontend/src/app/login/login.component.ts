import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {FormBuilder, FormGroup} from '@angular/forms';
import {AuthenticationService} from "../../services/authentication.service";


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  showError = false;
  messageSecondsVisible = 3;
  cas: boolean = true;

  constructor(private router: Router, private authenticationService: AuthenticationService, private formBuilder: FormBuilder) {
    this.loginForm = this.formBuilder.group({
      mail: '',
      password: ''
    });
  }

  ngOnInit(): void {
  }

  // value is a login form
  login(value: any): void {
    // on succes, navigate to the returned url, else show a message
    const that = this;
    this.authenticationService.login(value.mail, value.password, (url) => {
      that.router.navigateByUrl(url).then(r => {});
    }, () => {
      that.showErrorMessage();
      that.loginForm.reset();
    });
  }

  showErrorMessage(): void {
    this.showError = true;
    const that = this;
    setTimeout(() => {
      that.showError = false;
    }, this.messageSecondsVisible * 1000);
  }

}
