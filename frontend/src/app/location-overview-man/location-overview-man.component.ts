import {Component, OnInit} from '@angular/core';
import {transition, trigger, useAnimation} from "@angular/animations";
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {Subject} from "rxjs";
import {rowsAnimation} from "../animations";
import {ILocation} from "../../interfaces/ILocation";
import {LocationService} from "../../services/location.service";
import {AuthenticationService} from "../../services/authentication.service";
import {UserService} from "../../services/user.service";
import {TranslateService} from "@ngx-translate/core";
import {appLanguages, languageTranslations} from "../../environments/environment";

declare var $: any;

@Component({
  selector: 'app-location-overview-man',
  templateUrl: './location-overview-man.component.html',
  styleUrls: ['./location-overview-man.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class LocationOverviewManComponent implements OnInit {
  results: ILocation[];

  numbers: number[];
  lower: number;
  upper: number;
  linesOnPage = 20;
  reloadSubject: Subject<boolean> = new Subject<boolean>();

  selectedLocation: ILocation;
  selectedLocationCopy: ILocation;
  selectedName: string; // old name should be stored for put messages
  selectedIndex: number;

  defaultImageUrl = "https://everestgloballtd.com/frontend/images/gallery/default-corporate-image.jpg";
  displayDelete = 'none';
  errorAddLocationHidden: boolean = true;
  errorChangeLocationHidden: boolean = true;

  private allScanners: string[];
  potentialScanners: string[];
  currentScanners: string[];
  searchValue: any;
  searchedScanners: any[];
  scannerForm: FormGroup;
  private scanners: FormArray;

  locationForm = new FormGroup({
    name: new FormControl('', Validators.required),
    address: new FormControl('', Validators.required),
    imageUrl: new FormControl(''),
    mapsFrameUrl: new FormControl('',  [Validators.required, Validators.pattern('^https://www.google.com/maps/embed\?.*$')]),
    numberOfLockers: new FormControl('', Validators.min(0)),
    numberOfSeats: new FormControl('', [Validators.required, Validators.min(0)]),
  });

  changeLocationForm = new FormGroup({
    name: new FormControl('', Validators.required),
    address: new FormControl('', Validators.required),
    imageUrl: new FormControl(''),
    mapsFrameUrl: new FormControl('',  Validators.required),
    numberOfLockers: new FormControl('', Validators.min(0)),
    numberOfSeats: new FormControl('', [Validators.required, Validators.min(0)]),
  });


  Object: any;
  appLanguages: {};
  languageTranslations: {};
  public translations:{};

  constructor(private formBuilder: FormBuilder, private locationService: LocationService, public authenticationService: AuthenticationService, private userService: UserService,
              public translate: TranslateService) {
    this.results = [];
    this.lower = 0;
    this.upper = 19;
    this.scannerForm = this.formBuilder.group({
        scanners: this.formBuilder.array([])
    });
    this.Object = Object;
    this.appLanguages = appLanguages;
    this.languageTranslations = languageTranslations;

    let descriptions = new FormGroup({});
    for(let o of Object.keys(appLanguages)){
      descriptions.addControl(appLanguages[o], new FormControl('',  Validators.required));
    }
    this.locationForm.addControl('descriptions', descriptions);
    this.changeLocationForm.addControl('descriptions', descriptions);
  }

  ngOnInit(): void {
    this.locationService.getAllLocations().subscribe(value => {
      this.results = value;
      this.selectedLocation = value[0];
      this.numbers = Array(Math.ceil(value.length / 20)).fill(1).map((x, i) => i + 1);
      this.results.sort((a, b) => a.name.localeCompare(b.name));
    });

    this.userService.getUsersNamesByRole('EMPLOYEE').subscribe(value => {
      this.allScanners = value;
    });
    this.userService.getUsersNamesByRole('ADMIN').subscribe(value => {
      for(let l of value){
        if(this.allScanners.indexOf(l) < 0 ){
          this.allScanners.push(l);
        }
      }
    });
  }

  newPage(i) {
    this.lower = (i - 1) * 20;
    this.upper = (i - 1) * 20 + 19;

     /*let scrollToTop = window.setInterval(() => {
      let pos = window.pageYOffset;
      if (pos > document.getElementById("resultsTable").offsetTop - 70) {
        window.scrollTo(0, pos - 20); // how far to scroll on each step
      } else {
        window.clearInterval(scrollToTop);
      }
    }, 16);
      */
  }

  floor(i): number {
    return Math.floor(i);
  }

  addLocation(value: any): void {
    if (this.locationForm.valid) {
      const loc: ILocation = {
        imageUrl: value.imageUrl,
        mapsFrame: value.mapsFrameUrl !== null ? value.mapsFrameUrl : '',
        descriptions: {},
        calendar: null,
        lockers: null,
        address: value.address,
        name: value.name,
        numberOfLockers: value.numberOfLockers !== null && value.numberOfLockers !== "" ? value.numberOfLockers : 0,
        numberOfSeats: value.numberOfSeats,
        startPeriodLockers: null,
        endPeriodLockers: null,
      };

      for (let lang of Object.keys(appLanguages)) {
        loc.descriptions[appLanguages[lang]] = value.descriptions[appLanguages[lang]];
      }

      if (loc.imageUrl === undefined || loc.imageUrl === '') {
        loc.imageUrl = this.defaultImageUrl;
      }
      this.selectedName = this.locationForm.controls.name.value;

      this.locationService.addLocation(loc).toPromise().then(addedLocation  => {
        this.results.push(addedLocation);
        this.results.sort((a, b) => a.name.localeCompare(b.name));})
        .catch( err => {
          if(err.status == 400){
            this.errorAddLocationHidden = false;
            setTimeout(() => {
              this.errorAddLocationHidden = true;
            }, 6 * 1000);
          }
        });
      this.reloadSubject.next(true);
      this.locationForm.reset();
    } else {
      // when a field was left empty and untouched this loop will mark it as touched and this will trigger validation
      Object.keys(this.locationForm.controls).forEach(field => {
        const control = this.locationForm.get(field);
        control.markAsTouched({ onlySelf: true });
      });
      Object.keys(this.locationForm.controls["descriptions"]['controls']).forEach(field => {
        const control = this.locationForm.controls['descriptions']['controls'][field];
        control.markAsTouched({ onlySelf: true });
      });

    }
  }

  setSelectedLocation(selectedLocation: ILocation): void {
    this.selectedLocation = selectedLocation;
    this.selectedName = selectedLocation.name;
    this.selectedLocationCopy = {...selectedLocation}; // takes a copy of the location to revert if changes are cancelled
    this.selectedIndex = this.results.indexOf(selectedLocation);

    this.changeLocationForm.controls["name"].setValue(selectedLocation.name);
    this.changeLocationForm.controls["address"].setValue(selectedLocation.address);
    this.changeLocationForm.controls["imageUrl"].setValue(selectedLocation.imageUrl);
    this.changeLocationForm.controls["mapsFrameUrl"].setValue(selectedLocation.mapsFrame);
    this.changeLocationForm.controls["numberOfLockers"].setValue(selectedLocation.numberOfLockers);
    this.changeLocationForm.controls["numberOfSeats"].setValue(selectedLocation.numberOfSeats);
    for(let o of Object.keys(this.appLanguages)){
      this.changeLocationForm['controls'].descriptions['controls'][appLanguages[o]].setValue(selectedLocation.descriptions[appLanguages[o]]);
    }
    this.translations = this.selectedLocation.descriptions;

    this.searchValue = null;



    this.locationService.getScannersFromLocation(this.selectedName).subscribe(
      value => {
        this.currentScanners = value;
        this.potentialScanners = this.allScanners.filter(scanner => {
          return !this.currentScanners.includes(scanner)
        });
      }
    );
  }

  changeLocation(): void {
    if(this.changeLocationForm.valid){
      this.selectedLocation.name = this.changeLocationForm.controls["name"].value;
      this.selectedLocation.address = this.changeLocationForm.controls["address"].value;
      this.selectedLocation.address = this.changeLocationForm.controls["address"].value;
      this.selectedLocation.imageUrl = this.changeLocationForm.controls["imageUrl"].value;
      this.selectedLocation.mapsFrame = this.changeLocationForm.controls["mapsFrameUrl"].value;
      this.selectedLocation.numberOfLockers = this.changeLocationForm.controls["numberOfLockers"].value;
      this.selectedLocation.numberOfSeats = this.changeLocationForm.controls["numberOfSeats"].value;
      for(let l of Object.keys(appLanguages)){
        this.selectedLocation.descriptions[appLanguages[l]] = this.changeLocationForm.controls["descriptions"]['controls'][appLanguages[l]].value;
      }
      this.locationService.saveLocation(this.selectedName, this.selectedLocation).subscribe(
        (r) => {
          this.results.sort((a, b) => a.name.localeCompare(b.name));
          this.errorChangeLocationHidden = true;
          document.getElementById("closeChangeLocationModalBtn").click();
        }, (e) => {
          this.errorChangeLocationHidden = false;
        }
      );
      //document.getElementById("closeChangeLocationModalBtn").click();
    }else{
      // when a field was left empty and untouched this loop will mark it as touched and this will trigger validation
      Object.keys(this.changeLocationForm.controls).forEach(field => {
        const control = this.changeLocationForm.get(field);
        control.markAsTouched({ onlySelf: true });
      });
      Object.keys(this.changeLocationForm.controls["descriptions"]['controls']).forEach(field => {
        const control = this.changeLocationForm.controls['descriptions']['controls'][field];
        control.markAsTouched({ onlySelf: true });
      });
    }
  }

  changeLanguage(l: string, event: FocusEvent){
    this.changeLocationForm.controls.descriptions[appLanguages[l]] = event.target["value"];
  }

  cancel(event){
    event.preventDefault();
    this.locationForm.reset();
  }

  setBackLocation(): void{
    this.results[this.selectedIndex] = this.selectedLocationCopy;
    this.changeLocationForm.reset();
  }

  deleteLocation(name: string): void {
    this.locationService.deleteLocation(name).subscribe(n => {
      let idx = this.results.findIndex(l => name === l.name);
      if (idx > -1) {
        this.results.splice(idx, 1);
      }
    });
  }

  onChange(name: string, isChecked: boolean) {
    this.scanners = (this.scannerForm.controls.scanners as FormArray);

    if (isChecked) {
      this.scanners.push(new FormControl(name));
    } else {
      const index = this.scanners.controls.findIndex(x => x.value === name);
      this.scanners.removeAt(index);
    }
  }

  addScanners(): void{
    for(let s of this.scannerForm.value.scanners){
      this.currentScanners.push(s);
      this.potentialScanners = this.potentialScanners.filter(
        value => {
          return value != s;
        }
      );
      if(this.searchedScanners !== undefined && this.searchedScanners.length > 0){
        this.searchedScanners = this.searchedScanners.filter(
          value => {
            return value != s;
          }
        );
      }
    }
    this.scanners.clear();
    this.scannerForm.reset();
  }

  removeScanners(nameScanners: string[]): void {
    for(let s of nameScanners){
      this.removeScanner(s);
    }
  }


  removeScanner(nameScanner: string): void {
    this.currentScanners = this.currentScanners.filter(
      value => {
        return value != nameScanner;
      }
    );
    this.potentialScanners.push(nameScanner);
  }

  search(): void {
    this.searchedScanners = [];
    if(this.searchValue !== null){
      this.userService.getUsersNamesByRole('EMPLOYEE').subscribe( value => {
        for(let s of value){
          if(s.indexOf(this.searchValue.trim()) !== -1 && !this.currentScanners.includes(s)){
            this.searchedScanners.push(s);
          }
        }
        this.potentialScanners = this.allScanners.filter(scanner => {
          return !this.searchedScanners.includes(scanner) && !this.currentScanners.includes(scanner)
        });
      });
    }
  }

  //the changes on the backend only happen when the admin presses save
  saveScanners() {
    this.locationService.updateScanners(this.selectedName, this.currentScanners).subscribe(v => v=v );
  }
}

