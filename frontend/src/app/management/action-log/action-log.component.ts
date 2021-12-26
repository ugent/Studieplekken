import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { ActionLogEntry } from 'src/app/shared/model/ActionLogEntry';
import { ActionLogService } from 'src/app/services/api/action-log/action-log.service';
import { FormControl, FormGroup } from '@angular/forms';


@Component({
  selector: 'app-action-log',
  templateUrl: './action-log.component.html',
  styleUrls: ['./action-log.component.scss']
})
export class ActionLogComponent implements OnInit {

  formGroup = new FormGroup({
    searchFilter: new FormControl('')
  });

  allActions: ActionLogEntry[];
  searchFilter: string = "";
  actions: ActionLogEntry[];


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
        action.description.toLowerCase().includes(searchFilter) ||
        action.time.format('DD/MM/YYYY HH:mm:ss').toString().toLowerCase().includes(searchFilter);
      }
    );
    return ret;
  }

  filterChanged(formValue: {searchFilter: string}) {
    this.searchFilter = formValue.searchFilter;
  }

  ignore() {

  }

}
