import { Component, OnInit } from '@angular/core';
import {Observable} from 'rxjs';
import {LocationTag} from '../../shared/model/LocationTag';
import {TagsService} from '../../services/api/tags/tags.service';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../shared/animations/RowAnimation';

@Component({
  selector: 'app-tags-management',
  templateUrl: './tags-management.component.html',
  styleUrls: ['./tags-management.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class TagsManagementComponent implements OnInit {
  tagsObs: Observable<LocationTag[]>;

  successGettingTags: boolean = undefined;

  constructor(private tagsService: TagsService) { }

  ngOnInit(): void {
    this.tagsObs = this.tagsService.getAllTags();
    this.tagsObs.subscribe(
      () => {
        // Setting the 'successGettingTags' to true doesn't really do anything
        this.successGettingTags = true;
      }, () => {
        // But this does: gives feedback to the user
        this.successGettingTags = false;
      }
    );
  }

}
