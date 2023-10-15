import {Component, EventEmitter, Output} from '@angular/core';
import {BarcodeFormat} from '@zxing/library';
import {BehaviorSubject} from 'rxjs';

/**
 * ScannerComponent is based on the demo code provided by the authors of
 * @zxing/ngx-scanner (cfr. https://github.com/zxing-js/ngx-scanner)
 */
@Component({
    selector: 'app-scanner',
    templateUrl: './scanner.component.html',
    styleUrls: ['./scanner.component.scss']
})
export class ScannerComponent {

    availableDevices: MediaDeviceInfo[] = [];
    deviceCurrent: MediaDeviceInfo = null;
    deviceSelected: string;

    formatsEnabled: BarcodeFormat[] = [
        BarcodeFormat.CODE_128,
        BarcodeFormat.DATA_MATRIX,
        BarcodeFormat.EAN_13,
        BarcodeFormat.QR_CODE,
    ];

    hasDevices: boolean;
    hasPermission: boolean;

    @Output() scanSuccess = new EventEmitter<string>();

    torchEnabled = false;
    torchAvailable$ = new BehaviorSubject<boolean>(false);
    tryHarder = false;

    constructor() {
    }

    onCamerasFound(devices: MediaDeviceInfo[]): void {
        this.availableDevices = devices;
        this.hasDevices = Boolean(
            devices && devices.length
        );
    }

    onCodeResult(resultString: string): void {
        this.scanSuccess.emit(resultString);
    }

    onDeviceChange(device: MediaDeviceInfo): void {
        if (device?.deviceId !== this.deviceSelected) {
            this.deviceSelected = device?.deviceId;
            this.deviceCurrent = device || undefined;
        }
    }

    onHasPermission(has: boolean): void {
        this.hasPermission = has;
    }

    onTorchCompatible(isCompatible: boolean): void {
        this.torchAvailable$.next(isCompatible || false);
    }

    toggleTorch(): void {
        this.torchEnabled = !this.torchEnabled;
    }

    toggleTryHarder(): void {
        this.tryHarder = !this.tryHarder;
    }
}
