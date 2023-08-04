import {Component, Input, OnChanges, OnInit, TemplateRef} from '@angular/core';
import { Observable } from 'rxjs';
import { PenaltyList, PenaltyService } from 'src/app/extensions/services/api/penalties/penalty.service';
import { Penalty } from '../../../model/Penalty';
import { User } from '../../../model/User';
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-user-penalty-manager',
  templateUrl: './user-penalty-manager.component.html',
  styleUrls: ['./user-penalty-manager.component.scss']
})
export class UserPenaltyManagerComponent implements OnInit, OnChanges {

  @Input() user: User;
  @Input() showHeader = true;
  penalties: Observable<PenaltyList>;
  overview = true;
  addForm = false;
  model: {points: number, description: string} = {points: 0, description: ""};

  constructor(private penaltiesService: PenaltyService, private modalService: MatDialog) { }

  ngOnInit(): void {
  }

  ngOnChanges(): void {
    if (this.user && this.user.userId) {
      this.penalties = this.penaltiesService.getPenaltiesOfUserById(this.user.userId);
    }
  }

  addPenaltyButton(template: TemplateRef<any>): void {
      this.modalService.open(template);
  }

  addPenalty(): void {
    const penalty = new Penalty();
    penalty.designee = this.user;
    penalty.points = this.model.points;
    penalty.description = this.model.description;
    this.penaltiesService.addPenalty(penalty).subscribe();

    this.addForm = false;
    this.overview = true;
    this.penalties = this.penaltiesService.getPenaltiesOfUserById(this.user.userId);
  }

  cancelPenalty(): void {
    this.addForm = false;
    this.overview = true;
  }

  onDelete() {
    this.penalties = this.penaltiesService.getPenaltiesOfUserById(this.user.userId);
  }
}
