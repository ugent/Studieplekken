import {Component, Input, OnInit} from '@angular/core';
import {Observable} from "rxjs";
import {Location} from "../../../../shared/model/Location";

@Component({
  selector: 'app-lockers-table',
  templateUrl: './lockers-table.component.html',
  styleUrls: ['./lockers-table.component.css']
})
export class LockersTableComponent implements OnInit {
  @Input() location: Observable<Location>;

  constructor() { }

  ngOnInit(): void {
  }

}
