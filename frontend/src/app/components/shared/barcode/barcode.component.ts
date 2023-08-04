import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from 'src/app/model/User';

@Component({
  selector: 'app-barcode',
  templateUrl: './barcode.component.html',
  styleUrls: ['./barcode.component.scss'],
})
export class BarCodeComponent implements OnInit {

  @Input() userObs: Observable<User>

  constructor() { }

  ngOnInit(): void {
  }

}
