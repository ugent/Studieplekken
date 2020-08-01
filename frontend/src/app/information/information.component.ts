import { Component, OnInit } from '@angular/core';
import {LanguageService} from '../services/language/language.service';

@Component({
  selector: 'app-information',
  templateUrl: './information.component.html',
  styleUrls: ['./information.component.css']
})
export class InformationComponent implements OnInit {

  constructor(private languageService: LanguageService) { }

  ngOnInit(): void {
    console.log('assets/md/information.{{currentLanguage()}}.md');
  }

  currentLanguage(): string {
    return this.languageService.currentLanguage();
  }
}
