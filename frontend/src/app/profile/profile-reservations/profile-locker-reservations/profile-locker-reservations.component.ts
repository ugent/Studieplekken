import { Component, OnInit } from '@angular/core';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../../shared/animations/RowAnimation';

@Component({
  selector: 'app-profile-locker-reservations',
  templateUrl: './profile-locker-reservations.component.html',
  styleUrls: ['./profile-locker-reservations.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class ProfileLockerReservationsComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
