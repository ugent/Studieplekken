import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PenaltyList, PenaltyService } from 'src/app/services/api/penalties/penalty.service';

@Component({
  selector: 'app-penalties-management',
  templateUrl: './penalties-management.component.html',
  styleUrls: ['./penalties-management.component.scss']
})
export class PenaltiesManagementComponent implements OnInit {
  penaltyObservable: Observable<PenaltyList>;

  constructor(private penaltiesService: PenaltyService) { }

  ngOnInit(): void {
    this.penaltyObservable = this.penaltiesService.getAllPenalties().pipe(map(p => ({points: 1, penalties: p})));
  }

  onDelete(): void {
    this.penaltyObservable = this.penaltiesService.getAllPenalties().pipe(map(p => ({points: 1, penalties: p})));
  }

}
