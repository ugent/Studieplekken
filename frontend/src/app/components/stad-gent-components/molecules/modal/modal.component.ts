import { NgOptimizedImage } from '@angular/common';
import {Component, EventEmitter, OnInit, Output, TemplateRef, ViewChild} from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-modal',
    templateUrl: './modal.component.html',
    styleUrls: ['./modal.component.scss']
})
export class ModalComponent implements OnInit {

    @ViewChild('modal') protected modalElement: TemplateRef<any>;

    @Output() protected onModalOpen: EventEmitter<void>;
    @Output() protected onModalClose: EventEmitter<void>;

    private openedModal?: MatDialogRef<HTMLDivElement>;
    private openedModalCloseSubscription?: Subscription;

    constructor(private modalService: MatDialog) {
        this.onModalOpen = new EventEmitter<void>();
        this.onModalClose = new EventEmitter<void>();
    }

    public ngOnInit(): void {
    }

    /**
     * Opens the modal by using the modal service.
     */
    public open(): void {
        this.openedModal = this.modalService.open(this.modalElement);
        this.onModalOpen.emit();

        this.openedModalCloseSubscription = this.openedModal?.afterClosed().subscribe(() => {
            this.onModalClose.emit();
            this.openedModalCloseSubscription?.unsubscribe();
        });
    }

    /**
     * Closes the currently opened modal if it exists.
     */
    public close(): void {
        this.openedModal?.close();
    }
}
