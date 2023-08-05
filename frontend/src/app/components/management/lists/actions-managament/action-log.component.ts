import {Component, OnInit} from '@angular/core';
import {ActionLogEntry} from 'src/app/model/ActionLogEntry';
import {ActionLogService} from 'src/app/extensions/services/api/action-log/action-log.service';
import {ReplaySubject, Subject} from 'rxjs';
import {TableComponent} from '../../../../contracts/table.component.interface';
import {TableAction, TableMapper} from '../../../../model/Table';
import {FormatActionPipe} from '../../../../extensions/pipes/FormatActionPipe';


@Component({
    selector: 'app-actions-managament',
    templateUrl: './action-log.component.html',
    styleUrls: ['./action-log.component.scss']
})
export class ActionLogComponent implements OnInit, TableComponent {

    protected actionsObs$: Subject<ActionLogEntry[]>;

    constructor(
        private actionService: ActionLogService,
        private logFormatter: FormatActionPipe,
    ) {
        this.actionsObs$ = new ReplaySubject();
    }

    ngOnInit(): void {
        this.actionService.getAllActions().subscribe(actions =>
            this.actionsObs$.next(actions)
        );
    }

    getTableActions(): TableAction[] {
        return [];
    }

    getTableMapper(): TableMapper {
        return (log: ActionLogEntry) => ({
            'management.actionlog.table.type': log.type,
            'management.actionlog.table.domain': log.domain,
            'management.actionlog.table.description': this.logFormatter.transform(log),
            'management.actionlog.table.username': log.userFullName,
            'management.actionlog.table.time': log.time.format('YYYY/MM/DD h:mm:ss')
        });
    }
}
