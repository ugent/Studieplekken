import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { PenaltyList, PenaltyService } from 'src/app/services/api/penalties/penalty.service';
import { Penalty } from '../../model/Penalty';
import { User } from '../../model/User';

@Component({
  selector: 'app-user-penalty-manager',
  templateUrl: './user-penalty-manager.component.html',
  styleUrls: ['./user-penalty-manager.component.scss']
})
export class UserPenaltyManagerComponent implements OnInit {

  @Input() user: User;
  penalties: Observable<PenaltyList>;
  overview = true;
  addForm = false;
  model: {points: number, description: string} = {points: 0, description: ""};

  constructor(private penaltiesService: PenaltyService) { }

  ngOnInit(): void {
  }

  ngOnChanges() {
    if(this.user)
      this.penalties = this.penaltiesService.getPenaltiesOfUserById(this.user.userId);
  }

  addPenaltyButton() {
    this.addForm = true;
    this.overview = false;
  }

  addPenalty() {
    const penalty = new Penalty();
    penalty.designee = this.user;
    penalty.points = this.model.points;
    penalty.description = this.model.description;
    this.penaltiesService.addPenalty(penalty).subscribe();

    this.addForm = false;
    this.overview = true;
    this.penalties = this.penaltiesService.getPenaltiesOfUserById(this.user.userId);
  }

  onDelete() {
    this.penalties = this.penaltiesService.getPenaltiesOfUserById(this.user.userId);
  }
}
