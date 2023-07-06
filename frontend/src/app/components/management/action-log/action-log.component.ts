import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { ActionLogEntry } from 'src/app/extensions/model/ActionLogEntry';
import { ActionLogService } from 'src/app/extensions/services/api/action-log/action-log.service';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';


@Component({
  selector: 'app-action-log',
  templateUrl: './action-log.component.html',
  styleUrls: ['./action-log.component.scss']
})
export class ActionLogComponent implements OnInit {

  formGroup = new UntypedFormGroup({
    searchFilter: new UntypedFormControl('')
  });

  allActions: ActionLogEntry[];
  searchFilter: string = "";
  actions: ActionLogEntry[];
  currentSort: string = "domain";
  sortOrder: "asc" | "des"  = "asc";
  orderMarkers = {
    "type": "sort-both",
    "domain": "sort-both",
    "userFullName": "sort-both",
    "userId": "sort-both",
    "time": "sort-both"
  };


  constructor(private actionService: ActionLogService) {

  }

  ngOnInit(): void {
    this.actionService.getAllActions().subscribe(
      (actions) => {
        this.allActions = actions;
      }
    );
  }

  actionFilter(actions: ActionLogEntry[], searchFilter: string): ActionLogEntry[] {
    searchFilter = searchFilter.toLowerCase();
    const ret =  actions.filter(
      (action: ActionLogEntry) => {
        return action.type.toLowerCase().includes(searchFilter) ||
        action.userFullName.toLowerCase().includes(searchFilter) ||
        action.domain.toLowerCase().includes(searchFilter) ||
        action.type.toLowerCase().includes(searchFilter) ||
        action.time.format('DD/MM/YYYY HH:mm:ss').toString().toLowerCase().includes(searchFilter);
      }
    );
    return ret;
  }

  filterChanged(formValue: {searchFilter: string}) {
    this.searchFilter = formValue.searchFilter;
  }

  sortOn(val: string) {
    this.orderMarkers[this.currentSort] = "sort-both";
    if (this.currentSort == val) {
      if (this.sortOrder == "asc") {
        this.sortOrder = "des";
        this.orderMarkers[val] = "sort-up";
      } else {
        this.sortOrder = "asc";
        this.orderMarkers[val] = "sort-down";
      }

    } else {
      this.sortOrder = "des";
      this.orderMarkers[val] = "sort-up";
    }
    this.currentSort = val;
  }

  doSort(actions: ActionLogEntry[]): ActionLogEntry[] {
    const orderFactor = this.sortOrder == "asc"? 1 : -1;
    if (this.currentSort == "type") {
      return actions.sort((a, b) => {
        console.log(a, b);
        if (a.type > b.type) {
          return (-1) * orderFactor;
        }
        if (a.type < b.type) {
          return 1 * orderFactor;
        }
        return 0;
      });
    }
    if (this.currentSort == "domain") {
      return actions.sort((a,b ) => {
        if (a.domain > b.domain) {
          return (-1) * orderFactor;
        }
        if (a.domain < b.domain) {
          return 1 * orderFactor;
        }
        return 0;
      });
    }
    if (this.currentSort == "userFullName") {
      return actions.sort((a, b) => {
        if (a.userFullName > b.userFullName) {
          return (-1) * orderFactor;
        }
        if (a.userFullName < b.userFullName) {
          return 1 * orderFactor;
        }
        return 0;
      });
    }
    if (this.currentSort == "userId") {
      return actions.sort((a, b) => {
        if (a.user.userId > b.user.userId) {
          return (-1) * orderFactor;
        }
        if (a.user.userId < b.user.userId) {
          return 1 * orderFactor;
        }
        return 0;
      });
    }
    if (this.currentSort == "time") {
      return actions.sort((a, b) => {
        if (a.time.isAfter(b.time)) {
          return (-1) * orderFactor;
        }
        if (a.time.isBefore(b.time)) {
          return 1 * orderFactor;
        }
        return 0;
      });
    }
    return actions;
  }

  ignore() {

  }

}
