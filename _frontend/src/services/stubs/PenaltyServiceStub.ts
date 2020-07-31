import {Observable, of} from 'rxjs';
import {IPenaltyEvent} from '../../interfaces/IPenaltyEvent';
import {IPenalty} from '../../interfaces/IPenalty';

export default class PenaltyServiceStub{
  getAllPenaltyEvents(): Observable<IPenaltyEvent[]> {
    return of([]);
  }

  changePenaltyEvent(penaltyEvent: IPenaltyEvent): void {

  }

  addPenaltyEvent(penaltyEvent: IPenaltyEvent) {

  }

  getPenalties(): Observable<IPenalty[]>{
    return of([]);
  }

  deletePenaltyEvent(code: number): Observable<IPenaltyEvent>{
    return of(null);
  }
}
