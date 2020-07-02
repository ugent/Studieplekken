import { Injectable } from '@angular/core';
import {ILocationReservation} from '../interfaces/ILocationReservation';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';
import {baseHref, urls} from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
// service to handle websocket connection for the scan page
export class ScanService {

  constructor() { }

  private stompClient = null;

  connect(location: string, func?){
    let socket = new SockJS(baseHref + urls.websocketsScan);
    this.stompClient = Stomp.over(socket);
    this.stompClient.debug = null;
    this.stompClient.connect({}, (frame) => {
      // subscribes to an endpoint where scanned reservations will be sent to
      this.stompClient.subscribe('/reservationScans/' +
        location, reservation => {
        // place new entry at the top of the list
        func(reservation);
      });
    });
  }

  disconnect(){
    if (this.stompClient != null) {
      this.stompClient.disconnect();
    }
  }
}
