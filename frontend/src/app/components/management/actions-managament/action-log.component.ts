import {Component, OnInit} from '@angular/core';
import {ActionLogEntry} from 'src/app/model/ActionLogEntry';
import {ActionLogService} from 'src/app/services/api/action-log/action-log.service';
import {Observable, ReplaySubject} from 'rxjs';
import {TableComponent} from '@/contracts/table.component.interface';
import {TableAction, TableMapper} from '@/model/Table';
import {FormatActionPipe} from '@/helpers/pipes/FormatActionPipe';


@Component({
    selector: 'app-actions-managament',
    templateUrl: './action-log.component.html',
    styleUrls: ['./action-log.component.scss']
})
export class ActionLogComponent implements OnInit, TableComponent<ActionLogEntry> {

    protected actionsObs$: Observable<ActionLogEntry[]>;

    constructor(
        private actionService: ActionLogService,
        private logFormatter: FormatActionPipe,
    ) {
        this.actionsObs$ = new ReplaySubject();
    }

    ngOnInit(): void {
        this.actionsObs$ = this.actionService.getAllActions();
    }

    getTableActions(): TableAction<ActionLogEntry>[] {
        return [];
    }

    getTableMapper(): TableMapper<ActionLogEntry> {
        return (log: ActionLogEntry) => ({
            'management.actionlog.table.type': log.type,
            'management.actionlog.table.domain': log.domain,
            'management.actionlog.table.description': this.logFormatter.transform(log),
            'management.actionlog.table.username': log.userFullName,
            'management.actionlog.table.time': log.time.format('YYYY/MM/DD h:mm:ss')
        });
    }
}
