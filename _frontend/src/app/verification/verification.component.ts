import { Component, OnInit } from '@angular/core';
import {VerificationService} from "../../services/verification.service";

@Component({
  selector: 'app-verification',
  templateUrl: './verification.component.html',
  styleUrls: ['./verification.component.css']
})
export class VerificationComponent implements OnInit {

  constructor(public verificationService: VerificationService) { }

  // when a user has registred, he gets redirected to this page and instructed to go to
  // his mailbox to verify his account
  ngOnInit(): void {
    if (this.verificationService.getNewUser() === undefined) {
      let testUser = {
        email: 'example@ugent.be',
        name: 'Test',
        lastName: 'Example',
        birthDate: '1970-01-01',
        pwd: 'pwd',
        confPwd: 'pwd',
        institution: 'UGent',
        studentNr: '-1'
      };
      this.verificationService.setNewUser(testUser);
    }
  }

}
