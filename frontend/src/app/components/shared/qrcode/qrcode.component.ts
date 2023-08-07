import { Component, Input, OnInit } from '@angular/core';
import { User } from 'src/app/model/User';

@Component({
  selector: 'app-qrcode',
  templateUrl: './qrcode.component.html',
  styleUrls: ['./qrcode.component.scss']
})
export class QRCodeComponent implements OnInit {

  @Input() user: User;

  constructor() {
  }

  ngOnInit(): void {
  }

}
