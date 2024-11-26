import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';

@Component({
    selector: 'app-modal',
    templateUrl: './modal.component.html',
    styleUrls: ['./modal.component.scss']
})
export class ModalComponent implements OnInit {

    @ViewChild('modal') modalElement: TemplateRef<any>;

    private openedModal: MatDialogRef<HTMLDivElement>;

    constructor(private modalService: MatDialog) {
    }

    ngOnInit(): void {
    }

    open(): void {
        this.openedModal = this.modalService.open(
            this.modalElement
        );
    }

    close(): void {
        this.openedModal?.close();
    }
}
