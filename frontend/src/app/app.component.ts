import { Component } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Location} from "./shared/model/Location";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  locations: Observable<Location[]> = this.http.get<Location[]>('/api/locations');

  constructor(private http: HttpClient) {
  }

  getKeys(obj: {}) : string[] {
    return Object.keys(obj);
  }
}
