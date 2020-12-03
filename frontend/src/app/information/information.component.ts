import {Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-information',
  templateUrl: './information.component.html',
  styleUrls: ['./information.component.css']
})
export class InformationComponent implements OnInit {

  constructor(private translate: TranslateService) {
  }

  ngOnInit(): void {
    console.log('assets/md/information.{{currentLanguage()}}.md');
  }

  currentLanguage(): string {
    return this.translate.currentLang;
  }
}
