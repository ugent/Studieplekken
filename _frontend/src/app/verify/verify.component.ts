import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {urls} from "../../environments/environment";
import {BehaviorSubject} from "rxjs";
import {VerificationService} from "../../services/verification.service";


@Component({
  selector: 'app-verify',
  templateUrl: './verify.component.html',
  styleUrls: ['./verify.component.css']
})
export class VerifyComponent implements OnInit {

  succeeded = new BehaviorSubject<boolean>(false);

  // This page is used by new users to verify their new created account.
  // There will be a query parameter code that needs to be sent to backend.
  // If the backend succesfully has verified the account, there will be shown a message.
  constructor(private route: ActivatedRoute, private verificationService: VerificationService) {
    const code= this.route.snapshot.params.code;
    this.verificationService.verify(code).subscribe(value => {
      this.succeeded.next(true);
    });
  }

  ngOnInit(): void {
  }

}
