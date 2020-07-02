import { Component, OnInit } from '@angular/core';
import {LocationReservationService} from '../../services/location-reservation.service';
import {TranslateService} from '@ngx-translate/core';
import {AuthenticationService} from '../../services/authentication.service';
import {ActivatedRoute} from '@angular/router';

declare var $: any;

@Component({
  selector: 'app-scan-for-student',
  templateUrl: './scan-for-student.component.html',
  styleUrls: ['./scan-for-student.component.css']
})
export class ScanForStudentComponent implements OnInit {

  barcode: string;

  failMessage: string;
  successMessage: string;
  scanLocation: string;
  constructor(private locationReservationService: LocationReservationService, private translate: TranslateService,
              public authenticationService: AuthenticationService, private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.scanLocation = this.route.snapshot.paramMap.get('location');
    console.log(this.scanLocation);
  }


  ngAfterContentChecked(): void {
    $('.selectpicker').selectpicker('refresh');
  }

  check(barcode: string){
    this.locationReservationService.scanBarcode(this.scanLocation, barcode).then(val => {
      let m = (this.translate.currentLang === 'en' ? 'Student has been scanned!' : 'Student is gescand!');
      this.showSuccessMessage(m);

    }).catch(reason => {
      let  m = (this.translate.currentLang === 'en' ? 'Error: couldn\'t scan student ' : 'Fout: Student is niet gescand');
      this.showErrorMessage(m);
    });
    // reset focus of element
    document.getElementById('scanInput').focus();
    document.getElementById('scanInput')["value"] = "";
    this.barcode = "";
  }



  showErrorMessage(message: string){
    this.failMessage = message;
    document.getElementById('success_alert').style.display = 'none';
    document.getElementById('fail_alert').style.display = 'initial';
    setTimeout(() => {
      document.getElementById('fail_alert').style.display = 'none';
    }, 2000);
  }

  showSuccessMessage(message: string){
    this.successMessage = message;
    document.getElementById('success_alert').style.display = 'initial';
    document.getElementById('fail_alert').style.display = 'none';
    setTimeout(() => {
      document.getElementById('success_alert').style.display = 'none';
    }, 2000);
  }

}
