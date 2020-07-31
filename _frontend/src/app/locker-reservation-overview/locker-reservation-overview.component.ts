import {Component, OnInit} from '@angular/core';
import {transition, trigger, useAnimation} from "@angular/animations";
import {rowsAnimation} from "../animations";
import {ILockerReservation} from "../../interfaces/ILockerReservation";
import {ISearchPossibility} from "../../interfaces/ISearchPossibility";
import {LockerReservationService} from "../../services/locker-reservation.service";
import {TranslateService} from "@ngx-translate/core";
import {ILocationReservation} from '../../interfaces/ILocationReservation';

// necessary to use jquery, import $ from 'jquery' does not work!
declare let $: any;

@Component({
  selector: 'app-locker-reservation-overview',
  templateUrl: './locker-reservation-overview.component.html',
  styleUrls: ['./locker-reservation-overview.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class LockerReservationOverviewComponent implements OnInit {

  linesOnPage: number;
  lower: number;
  upper: number;
  numbers: number[];
  results: ILockerReservation[];
  searchPossibilities: ISearchPossibility[];

  filteredResults: ILockerReservation[];
  uniqueNames = new Set<String>();
  uniqueLocations = new Set<String>();

  filters= { name: false, locationName: false, startDate: false, endDate: false};


  constructor(public lockerReservationService: LockerReservationService, private translate: TranslateService) {

    // changes calendar picker language when the translateService language changes
    translate.onLangChange.subscribe(value => {
      $(function () {
        if($('#datetimepicker1') != undefined && $('#datetimepicker1').data("DateTimePicker") != undefined){
          $('#datetimepicker1').data("DateTimePicker").locale(value.lang);
          $('#datetimepicker2').data("DateTimePicker").locale(value.lang);
        }
      });
    });

    // initializes the datetimepickers
    let locale = this.translate.currentLang;
    $(function () {
      $('#datetimepicker1').datetimepicker({
        format: 'DD-MM-YYYY',
        locale: locale
      });
      $('#datetimepicker2').datetimepicker({
        useCurrent: false,
        format: 'DD-MM-YYYY',
        locale: locale
      });
      // change second date so it should always be >= first date
      $("#datetimepicker1").on("dp.change", function (e) {
        $('#datetimepicker2').data("DateTimePicker").minDate(e.date);
      });

      // change first date so it should always be <= second date
      $("#datetimepicker2").on("dp.change", function (e) {
        $('#datetimepicker1').data("DateTimePicker").maxDate(e.date);
      });
    });

  }

  ngOnInit(): void {
    this.linesOnPage = 20;
    this.lower = 0;
    this.upper = 19;
    this.results = [];
    this.searchPossibilities = [{
      name: "location",
      translation: "management.locationName",
      searchFunction: "getAllLockerReservationsOfLocation"
    },
      {
        name: "studentid",
        translation: "management.studentid",
        searchFunction: "getAllLockerReservationsOfUser"
      },
      {
        name: "studentName",
        translation: "management.studentName",
        searchFunction: "getAllLockerReservationsOfUserByName"
      },
    ]
  }

  // necessary for selectpicker, makes it so items appear correct
  ngAfterContentChecked(): void {
    $('.selectpicker').selectpicker('refresh');
  }


  resultsChanged(results: ILockerReservation[]) {
    this.numbers = Array(Math.ceil(results.length / 20)).fill(1).map((x, i) => i + 1);
    this.results = results;
    this.filteredResults = results;

    this.uniqueNames = new Set(this.results.map(item => item.owner.firstName + " " + item.owner.lastName));
    this.uniqueLocations = new Set(this.results.map(item => item.locker.location));
    // open up filter window
    $(".collapse").collapse("show");
  }

  newPage(i) {
    this.lower = (i - 1) * 20;
    this.upper = (i - 1) * 20 + 19;

    let scrollToTop = window.setInterval(() => {
      let pos = window.pageYOffset;
      if (pos > document.getElementById("resultsTable").offsetTop - 70) {
        window.scrollTo(0, pos - 20); // how far to scroll on each step
      } else {
        window.clearInterval(scrollToTop);
      }
    }, 16);
  }

  floor(i) {
    return Math.floor(i);
  }

  names: Set<string>;
  locationNames: Set<string>;
  startDate: number[];
  endDate: number[];

  applyFilter(): void {
    this.names = new Set<string>($('#select1').val().map(item => item));
    this.locationNames = new Set<string>($('#select2').val().map(item => item));

    // format DD-MM-YYYY
    this.startDate = document.getElementById('datepicker1')['value'].split('-');
    this.endDate= document.getElementById('datepicker2')['value'].split('-');

    this.filters["name"] = this.names.size < this.uniqueNames.size;
    this.filters["locationName"] = this.locationNames.size < this.uniqueLocations.size;
    this.filters["startDate"] = this.startDate.length > 1;
    this.filters["endDate"] = this.endDate.length > 1;
    this.filteredResults = [];

    for(let item of this.results){
      if(this.customFilter(item)){
        this.filteredResults.push(item);
      }
    }

    this.numbers = Array(Math.ceil(this.filteredResults.length / 20)).fill(1).map((x, i) => i + 1);
  }

  customFilter(item: ILockerReservation): boolean {
    if(this.filters["name"] && !this.names.has(item.owner.firstName + ' ' + item.owner.lastName)){
      return false;
    }
    if(this.filters["locationName"] && !this.locationNames.has(item.locker.location)){
      return false;
    }

    if(this.filters["startDate"] && !(item.startDate.year > this.startDate[2] || (item.startDate.year == this.startDate[2] && item.startDate.month > this.startDate[1])
      || (item.startDate.year == this.startDate[2] && item.startDate.month == this.startDate[1] && item.startDate.day >= this.startDate[0]))){
      return false;
    }
    if(this.filters["endDate"] && !(item.startDate.year < this.endDate[2] || (item.startDate.year == this.endDate[2] && item.startDate.month < this.endDate[1])
      || (item.startDate.year == this.endDate[2] && item.startDate.month == this.endDate[1] && item.startDate.day < this.endDate[0]))){
      return false;
    }
    return true;
  }

  removeFilter(): void {
    // reset filter values
    document.getElementById("datepicker1")["value"] = "";
    document.getElementById("datepicker2")["value"] = "";
    $('#select1').selectpicker('selectAll');
    $('#select2').selectpicker('selectAll');

    this.filteredResults = this.results;
    this.numbers = Array(Math.ceil(this.results.length / 20)).fill(1).map((x, i) => i + 1);
  }

}
