import {Component, OnInit} from '@angular/core';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../animations';
import {baseHref, roles, urls} from '../../environments/environment';
import {ILocationReservation} from '../../interfaces/ILocationReservation';
import {IRoles} from '../../interfaces/IRoles';
import {LocationReservationService} from '../../services/location-reservation.service';
import {AuthenticationService} from '../../services/authentication.service';
import {LocationService} from '../../services/location.service';
import { getToday} from '../../interfaces/CustomDate';
import {TranslateService} from '@ngx-translate/core';
import {ScanService} from '../../services/scan.service';
import {IUser} from "../../interfaces/IUser";

declare var $: any;

@Component({
  selector: 'app-scan',
  templateUrl: './scan.component.html',
  styleUrls: ['./scan.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class ScanComponent implements OnInit {

  scanLocation: string;
  roles: IRoles;

  locationNames: string[];
  open: boolean;
  failMessage: string;
  successMessage: string;
  mailAddresses: Set<string>;

  showAbsent: boolean;
  absentStudents: ILocationReservation[];
  presentStudents: ILocationReservation[];

  visibleStudents: ILocationReservation[];

  locationIsClosed: boolean;

  firstNameFilterValue = "";
  lastNameFilterValue = "";

  // this variable holds a reference to the separate page with view for students, so we can automatically close it when then scanning is done
  pageForStudents;


  constructor(private locationReservationService: LocationReservationService,
              public authenticationService: AuthenticationService, private locationService: LocationService,
              private translate: TranslateService, private scanService: ScanService) {
    this.roles = roles;
    this.open = false;
    this.mailAddresses = new Set<string>();
    this.locationNames = [];


    this.absentStudents = [];
    this.presentStudents = [];
    this.visibleStudents = [];

  }

  ngOnInit(): void {
    this.locationService.getAllAuthenticatedLocations(this.authenticationService.getCurrentUser().mail).subscribe(value => {
      for (let loc of value) {
        this.locationNames.push(loc);
      }
      this.locationNames.sort();
    });

  }

  ngOnDestroy(): void {
    this.scanService.disconnect();
    if(this.pageForStudents !== null && this.pageForStudents !== undefined){
      this.pageForStudents.close();
    }
  }

  // necessary to avoid selectpicker disappearing after switching pages
  ngAfterContentChecked(): void {
    $('.selectpicker').selectpicker('refresh');
  }

  // sets up a websocket connection with backend
  connect() {
    if (this.scanLocation != '' && this.scanLocation != undefined) {
      this.open = true;
      this.scanService.connect(this.scanLocation, (locationReservation) => {
        let l = JSON.parse(locationReservation.body) as ILocationReservation;
        this.presentStudents.unshift(l);
        this.absentStudents = this.absentStudents.filter(item => item.user.augentID != l.user.augentID );
        this.visibleStudents = this.showAbsent ? this.absentStudents : this.presentStudents;
      });

      this.locationReservationService.getAllAbsentLocationReservations(this.scanLocation, getToday()).subscribe(value => {
        this.absentStudents = value;
        this.absentStudents.sort();
        this.visibleStudents = this.absentStudents;
      });

      this.locationReservationService.getAllPresentLocationReservations(this.scanLocation , getToday()).subscribe(value => {
        this.presentStudents = value;
        this.presentStudents.sort();
      });

      this.showAbsent = true;
    } else {
      let m = this.translate.currentLang == 'en' ? 'Please select a location' : 'Duid een locatie aan';
      this.showErrorMessage(m);
    }
  }

  // closes the websocket connection with the backend
  disconnect() {
    this.open = false;
    // disconnect to the websocket service
    this.scanService.disconnect();
    // close the scanpage of students
    if(this.pageForStudents !== null && this.pageForStudents !== undefined) {
      this.pageForStudents.close();
      this.pageForStudents = null;
    }

    this.locationIsClosed = true;
    // set the mailAddresses to all mails of the absent students
    for(let l of this.absentStudents){
      this.mailAddresses.add(l.user.mail);
    }
  }

  check(barcode: string): void {
  this.locationReservationService.scanBarcode(this.scanLocation,  barcode).then(val => {
      let m = (this.translate.currentLang === 'en' ? 'Student has been scanned!' : 'Student is gescand!');
    }).catch(reason => {
      let  m = (this.translate.currentLang === 'en' ? 'Error: couldn\'t scan student ' : 'Fout: Student is niet gescand');
      this.showErrorMessage(m + ": " + reason);
    });

  }

  // in case something goes wrong with the scanning process this function can be called to indicate that there has been no scanning that day
  // it will mark all students of the selected location as attended so no one will receive undeserved penalty points
  cancelReservations(): void {
    this.locationReservationService.setAllStudentsOfLocationToAttended(this.scanLocation);
    this.presentStudents.push(...this.absentStudents);
    this.absentStudents = [];
    this.showAbsent = false;
  }

  cancelMail(mail: string): void {
    if (this.mailAddresses.has(mail)) {
      this.mailAddresses.delete(mail);
    } else {
      this.mailAddresses.add(mail);
    }
  }

  // sends mails and adds penalty points to all absent students
  sendMailsToAbsentStudents(): void {
    this.locationReservationService.sendMailsToAbsentStudents(this.mailAddresses);
  }

  setReservationsToUnattendedAndAddPenaltyPoints(): void {
    let ids = new Set<string>();
    for(let l of this.absentStudents){
      ids.add(l.user.augentID);
    }
    this.locationReservationService.setReservationsToUnattendedAndAddPenaltyPoints(ids, this.scanLocation);
  }

  setStudentToAbsent(augentId: string){
    this.locationReservationService.setReservationToUnattended(this.scanLocation, augentId);
    let user = this.presentStudents.find(item => item.user.augentID == augentId);
    this.presentStudents = this.presentStudents.filter(item => item.user.augentID != augentId);
    this.absentStudents.push(user);
    this.visibleStudents = this.presentStudents;
  }

  openPageForStudents(){
    this.pageForStudents = window.open(baseHref + urls.scanPageForStudent +this.scanLocation);
  }

  applyFirstNameFilter(firstName: string){
    if(this.showAbsent){
      this.visibleStudents = this.absentStudents.filter(item => item.user.firstName.includes(firstName));
    }else{
      this.visibleStudents = this.presentStudents.filter(item => item.user.firstName.includes(firstName));
    }
  }

  applyLastNameFilter(lastName: string){
    if(this.showAbsent){
      this.visibleStudents = this.absentStudents.filter(item => item.user.lastName.includes(lastName));
    }else{
      this.visibleStudents = this.presentStudents.filter(item => item.user.lastName.includes(lastName));
    }
  }

  showErrorMessage(message: string){
    try{
      this.failMessage = message;
      document.getElementById('success_alert').style.display = 'none';
      document.getElementById('fail_alert').style.display = 'initial';
      document.getElementById('messages').style.display = 'initial';

      setTimeout(() => {
        document.getElementById('fail_alert').style.display = 'none';
        document.getElementById('messages').style.display = 'none';
      }, 2000);
    } catch (TypeError) {

    }

  }

  showSuccessMessage(message: string){
    try{
      this.successMessage = message;
      document.getElementById('success_alert').style.display = 'initial';
      document.getElementById('fail_alert').style.display = 'none';
      document.getElementById('messages').style.display = 'initial';
      setTimeout(() => {
        document.getElementById('success_alert').style.display = 'none';
        document.getElementById('messages').style.display = 'none';
      }, 2000);
    } catch(TypeError) {

    }

  }

}
