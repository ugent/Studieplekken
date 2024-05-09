import {
    Component,
    EventEmitter,
    Input,
    OnInit,
    Output,
    OnChanges,
    SimpleChanges
} from '@angular/core';
import {CalendarView, CalendarEvent, CalendarDateFormatter} from 'angular-calendar';
import {BehaviorSubject, merge, Observable, of, Subject} from 'rxjs';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {defaultOpeningHour, defaultClosingHour} from 'src/app/app.constants';
import * as moment from 'moment';
import {BreakpointObserver} from '@angular/cdk/layout';
import {map} from 'rxjs/operators';
import {CustomDateFormatter} from '@/util/CustomDateFormatter';

@Component({
    selector: 'app-calendar',
    templateUrl: './calendar.component.html',
    styleUrls: ['./calendar.component.scss'],
    providers: [
        {
            provide: CalendarDateFormatter,
            useClass: CustomDateFormatter
        }
    ]
})
export class CalendarComponent implements OnInit, OnChanges {
    view: CalendarView = CalendarView.Week;
    viewDate: Date = new Date();

    CalendarView = CalendarView;

    openingHour: number;
    closingHour: number;

    @Input() events: CalendarEvent[];
    @Input() refresh: Subject<unknown>;

    @Input() currentEventTime: moment.Moment;
    @Output()
    currentEventTimeChange = new EventEmitter<moment.Moment>();

    @Output()
    calendarViewStyle = new EventEmitter<CalendarView>();

    @Output()
    timeslotPickedEvent: EventEmitter<any> = new EventEmitter<any>();

    @Output()
    hourPickedEvent: EventEmitter<moment.Moment> = new EventEmitter();

    eventsSubj: BehaviorSubject<CalendarEvent[]> = new BehaviorSubject<
        CalendarEvent[]
    >([]);

    currentLang: string;

    constructor(
        private translate: TranslateService,
        private breakpointObserver: BreakpointObserver
    ) {
        this.translate.onLangChange.subscribe(() => {
            this.currentLang = this.translate.currentLang;
        });
    }

    ngOnInit(): void {
        this.currentLang = this.translate.currentLang;
        this.eventsSubj.subscribe((next) => {
            this.changeCalendarSize(next);
        });

        this.setView(this.breakpointObserver.isMatched('(max-width: 500px)') ? CalendarView.Day : CalendarView.Week);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.events) {
            this.eventsSubj.next(this.events);
        }

        if (changes.currentEventTime && !!changes.currentEventTime.currentValue) {
            this.viewDate = changes.currentEventTime.currentValue.toDate();
        }
    }

    changeCalendarSize(events: CalendarEvent[]): void {
        if (events.length > 0) {
            const endHour = this.events
                .map((event) => event.end?.getHours())
                .reduce((a, b) => Math.max(a, b));
            const beginHour = this.events
                .map((event) => event.start.getHours())
                .reduce((a, b) => Math.min(a, b));
            this.openingHour =
                beginHour < defaultOpeningHour ? beginHour : defaultOpeningHour;
            this.closingHour =
                endHour > defaultClosingHour ? endHour : defaultClosingHour;
        } else {
            this.openingHour = defaultOpeningHour;
            this.closingHour = defaultClosingHour;
        }
    }

    handleEvent(action: string, event: CalendarEvent): void {
        this.timeslotPickedEvent.emit(event.meta);
    }

    setView(view: CalendarView): void {
        this.view = view;
        this.calendarViewStyle.next(view);
    }

    hourSegment(event: any): void {
        this.hourPickedEvent.next(moment(event.date));
    }

    emitDate(event: any): void {
        this.currentEventTimeChange.next(moment(event));
    }

    currentLanguage(): Observable<string> {
        return merge<LangChangeEvent, LangChangeEvent>(
            of<LangChangeEvent>({
                lang: this.translate.currentLang,
            } as LangChangeEvent),
            this.translate.onLangChange
        ).pipe(map((s) => s.lang));
    }
}
