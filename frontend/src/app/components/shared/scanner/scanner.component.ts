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

    @Output() scanSuccess = new EventEmitter<string>();

    protected availableDevices: MediaDeviceInfo[] = [];
    protected deviceCurrent: MediaDeviceInfo = null;
    protected deviceSelected: string;

    protected formatsEnabled: BarcodeFormat[] = [
        BarcodeFormat.CODE_128,
        BarcodeFormat.DATA_MATRIX,
        BarcodeFormat.EAN_13,
        BarcodeFormat.QR_CODE,
    ];

    protected hasDevices: boolean = false;
    protected hasPermission: boolean = false;
    protected isEnabled: boolean = false;
    protected isTorchEnabled: boolean = false;
    protected isTryHarder: boolean = false;

    /**
     * Handles the event when cameras are found.
     * 
     * @param devices - An array of MediaDeviceInfo objects representing the available camera devices.
     */
    public onCamerasFound(devices: MediaDeviceInfo[]): void {
        this.availableDevices = devices;
        this.hasDevices = devices && devices.length > 0;
    }

    /**
     * Handles the result of a successful scan and emits the result string.
     *
     * @param resultString - The string result obtained from the scan.
     * @emits scanSuccess - Emits the result string when a scan is successful.
     */
    public onCodeResult(resultString: string): void {
        this.scanSuccess.emit(resultString);
    }

    /**
     * Handles the change of the selected media device.
     * 
     * @param device - The media device information object.
     *                 If the device's ID is different from the currently selected device,
     *                 it updates the selected device and the current device.
     */
    public onDeviceChange(device: MediaDeviceInfo): void {
        if (device?.deviceId !== this.deviceSelected) {
            this.deviceSelected = device?.deviceId;
            this.deviceCurrent = device || undefined;
        }
    }

    /**
     * Sets the permission status.
     * 
     * @param has - A boolean indicating whether the permission is granted.
     */
    public onHasPermission(has: boolean): void {
        this.hasPermission = has;
    }

    /**
     * Handles the compatibility of the torch feature.
     * 
     * @param isCompatible - A boolean indicating whether the torch feature is compatible.
     */
    public onTorchCompatible(isCompatible: boolean): void {
        this.isTorchEnabled = isCompatible;
    }

    /**
     * Toggles the state of the torch (flashlight) on or off.
     */
    public toggleTorch(): void {
        this.isTorchEnabled = !this.isTorchEnabled;
    }

    /**
     * Toggles the `tryHarder` property between `true` and `false`.
     */
    public toggleTryHarder(): void {
        this.isTryHarder = !this.isTryHarder;
    }

    /**
     * Enables the scanner by setting the `isEnabled` property to `true`.
     */
    public enableScanner(): void {
        this.isEnabled = true;
    }

    /**
     * Disables the scanner by setting the `isEnabled` property to `false`.
     * This method can be used to stop the scanner from functioning.
     */
    public disableScanner(): void {
        this.isEnabled = false;
    }
}
