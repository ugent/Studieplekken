import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import Quagga, { QuaggaJSConfigObject, QuaggaJSResultObject } from '@ericblade/quagga2';

@Component({
  selector: 'app-scanner',
  templateUrl: './scanner.component.html',
  styleUrls: ['./scanner.component.css']
})
export class ScannerComponent implements OnInit, OnDestroy {
    @Input()
    validator?: (a: string) => boolean;
    @Output()
    code: EventEmitter<string> = new EventEmitter();

    constructor() { }

    ngOnInit(): void {

      console.log(window.innerWidth)

      const state: QuaggaJSConfigObject = {
      inputStream: {
        type: 'LiveStream',
        constraints: {
            width: Math.min(600, window.innerWidth * 0.7),
            height: Math.min(600, window.innerHeight * 0.7),
            facingMode: 'environment' // or user
        }
      },
      locator: {patchSize: 'medium', halfSample: true},
      frequency: 10,
      decoder: {readers: ['upc_reader', 'upc_e_reader']},
    };

      Quagga.init(state, (err) => {
        if (err) {
          console.log(err);
          return;
        }
        console.log('Initialization finished. Ready to start');
        Quagga.start();
      });

      Quagga.onDetected((c) => this.detected(c));
    }

    ngOnDestroy(): void {
      Quagga.stop();
    }

    private detected(code: QuaggaJSResultObject): void {
      const userCode: string = code.codeResult.code;

      if (!this.validator || this.validator(userCode)) {
        this.code.next(userCode);
      }
    }
}
