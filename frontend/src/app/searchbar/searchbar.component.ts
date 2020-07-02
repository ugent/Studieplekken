import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ISearchPossibility} from "../../interfaces/ISearchPossibility";

@Component({
  selector: 'app-searchbar',
  templateUrl: './searchbar.component.html',
  styleUrls: ['./searchbar.component.css']
})
export class SearchbarComponent implements OnInit {

  searchValue: string;
  searchFor: string;
  resultsVisible: boolean;
  results: [];
  error: string;
  errorHidden: boolean;

  // the service used for requesting data from backend e.g. LockerReservationService for requesting data about LockerReservations
  @Input() service;

  // the different values a user can search for including the variable name for translation
  // and the name of the function of the service to search for this value
  @Input() searchPossibilities: ISearchPossibility[];

  // this wil emit an event every time the results change, this event should be captured in the parent component
  @Output() resultsChanged = new EventEmitter<[]>();

  constructor() { }

  ngOnInit(): void {
    this.results = [];
    this.searchFor = this.searchPossibilities[0].name;
  }

  search(): void {
    for(let possibility of this.searchPossibilities){
      if (this.searchFor === possibility.name) {
        this.service[possibility.searchFunction](this.searchValue).subscribe(value => {
          if(value === null || value == undefined){
            this.results = [];
          }
          else if(Array.isArray(value)){
            this.results = value as [];
          }
          else{
            this.results = [];
            // @ts-ignore
            this.results.push(value);
          }
          this.resultsChanged.emit(this.results);
        }, error => {
          this.results = [];
          this.resultsChanged.emit(this.results);
          if(error.status = 404){
            this.error = error.error;
            this.errorHidden = false;
            setTimeout(() => {
              this.errorHidden = true;
            }, 5 * 1000);
          }
        });
      }
    }
    this.resultsVisible = true;
  }

}
