import {Component, OnInit} from '@angular/core';
import {LocationReservationService} from '../../services/location-reservation.service';
import {ILocationReservation} from '../../interfaces/ILocationReservation';
import {animate, sequence, style, transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../animations';
import {ISearchPossibility} from '../../interfaces/ISearchPossibility';
import {FormControl, FormGroup} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {of} from 'rxjs';

// to use jquery, import $ from 'jquery' does not work!
declare let $: any;

@Component({
  selector: 'app-location-reservation-overview',
  templateUrl: './location-reservation-overview.component.html',
  styleUrls: ['./location-reservation-overview.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class LocationReservationOverviewComponent implements OnInit {

  linesOnPage: number;
  lower: number;
  upper: number;
  numbers: number[];
  results: ILocationReservation[];
  filteredResults: ILocationReservation[];

  searchPossibilities: ISearchPossibility[];
  uniqueNames = new Set<String>();
  uniqueLocations = new Set<String>();

  filters= { name: false, locationName: false, startDate: false, endDate: false};

  constructor(public locationReservationService: LocationReservationService, private translate: TranslateService) {
    this.searchPossibilities = [{
      name: 'locationName',
      searchFunction: 'getAllLocationReservationsOfLocationByName',
      translation: 'management.locationName'
    },
      {
        name: 'studentid',
        searchFunction: 'getAllLocationReservationsOfUser',
        translation: 'management.studentid'
      },
      {
        name: 'studentName',
        searchFunction: 'getAllLocationReservationsOfUserByName',
        translation: 'management.studentName'
      }];

    // changes calendar picker language when the translateService language changes
    translate.onLangChange.subscribe(value => {
      $(function() {
        if($('#datetimepicker1') != undefined && $('#datetimepicker1').data('DateTimePicker')!= undefined){
          $('#datetimepicker1').data('DateTimePicker').locale(value.lang);
          $('#datetimepicker2').data('DateTimePicker').locale(value.lang);
        }
      });
    });

    // initializes the datetimepickers
    let locale = this.translate.currentLang;
    $(function() {
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
      $('#datetimepicker1').on('dp.change', function(e) {
        $('#datetimepicker2').data('DateTimePicker').minDate(e.date);
      });

      // change first date so it should always be <= second date
      $('#datetimepicker2').on('dp.change', function(e) {
        $('#datetimepicker1').data('DateTimePicker').maxDate(e.date);
      });
    });

    this.numbers = [];
  }

  // necessary for selectpicker, makes it so items appear correct
  ngAfterContentChecked(): void {
    $('.selectpicker').selectpicker('refresh');
  }

  updateResults(results: []) {
    this.numbers = Array(Math.ceil(results.length / 20)).fill(1).map((x, i) => i + 1);
    this.results = results;
    this.filteredResults = results;
    this.sortResults();

    this.uniqueNames = new Set(this.results.map(item => item.user.firstName + ' ' + item.user.lastName));
    this.uniqueLocations = new Set(this.results.map(item => item.location.name));

    // open up filter window
    $('.collapse').collapse('show');
    $('.selectpicker').selectpicker('refresh');

  }

  ngOnInit(): void {
    this.linesOnPage = 20;
    this.lower = 0;
    this.upper = 19;
    this.results = [];
  }

  sortResults(): void {
    this.filteredResults.sort((res1, res2) => {
      return res2.date.year * 365 + res2.date.month * 31 + res2.date.day - res1.date.year * 365 - res1.date.month * 31 - res1.date.day;
    });
  }

  newPage(i) {
    this.lower = (i - 1) * 20;
    this.upper = (i - 1) * 20 + 19;
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

  customFilter(item: ILocationReservation): boolean {
      if(this.filters["name"] && !this.names.has(item.user.firstName + ' ' + item.user.lastName)){
        return false;
      }
      if(this.filters["locationName"] && !this.locationNames.has(item.location.name)){
        return false;
      }
      if(this.filters["startDate"] && !(item.date.year > this.startDate[2] || (item.date.year == this.startDate[2] && item.date.month > this.startDate[1])
        || (item.date.year == this.startDate[2] && item.date.month == this.startDate[1] && item.date.day >= this.startDate[0]))){
        return false;
      }
      if(this.filters["endDate"] && !(item.date.year < this.endDate[2] || (item.date.year == this.endDate[2] && item.date.month < this.endDate[1])
        || (item.date.year == this.endDate[2] && item.date.month == this.endDate[1] && item.date.day < this.endDate[0]))){
        return false;
      }
      return true;
  }


  removeFilter(): void {
    // reset filter values
    document.getElementById('datepicker1')['value'] = '';
    document.getElementById('datepicker2')['value'] = '';
    $('#select1').selectpicker('selectAll');
    $('#select2').selectpicker('selectAll');

    this.filteredResults = this.results;

    this.numbers = Array(Math.ceil(this.results.length / 20)).fill(1).map((x, i) => i + 1);
  }

}
