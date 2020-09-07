import { Component, OnInit } from '@angular/core';
import {ApplicationTypeFunctionalityService} from '../services/functionality/application-type/application-type-functionality.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  showPenalties: boolean;

  constructor(private functionalityService: ApplicationTypeFunctionalityService) { }

  ngOnInit(): void {
    this.showPenalties = this.functionalityService.showPenaltyFunctionality();
  }

}
