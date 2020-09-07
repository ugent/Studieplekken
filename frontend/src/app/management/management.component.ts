import { Component, OnInit } from '@angular/core';
import {ApplicationTypeFunctionalityService} from '../services/functionality/application-type/application-type-functionality.service';

@Component({
  selector: 'app-management',
  templateUrl: './management.component.html',
  styleUrls: ['./management.component.css']
})
export class ManagementComponent implements OnInit {
  showPenalties: boolean;

  constructor(private functionalityService: ApplicationTypeFunctionalityService) { }

  ngOnInit(): void {
    this.showPenalties = this.functionalityService.showPenaltyFunctionality();
  }

}
