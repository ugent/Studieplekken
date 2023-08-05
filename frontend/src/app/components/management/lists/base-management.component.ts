import {Directive, OnDestroy, OnInit, TemplateRef, ViewChild} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { BehaviorSubject, Observable, ReplaySubject, Subject, Subscription } from 'rxjs';
import { FormGroup } from '@angular/forms';
import {TableComponent} from '../../../contracts/table.component.interface';
import {DeleteAction, EditAction, TableAction, TableMapper} from '../../../model/Table';
import {User} from '../../../model/User';
import {ModalComponent} from '../../stad-gent-components/molecules/modal/modal.component';

@Directive()
export abstract class BaseManagementComponent<T extends object> implements OnInit, OnDestroy, TableComponent {

    @ViewChild('modify') modifyModal: ModalComponent;
    @ViewChild('remove') deleteModal: ModalComponent;

    protected selectedSub: Subject<T>;

    protected isSuccess: Subject<boolean>;
    protected feedbackMessage: Subject<string>;

    protected formGroup: FormGroup;

    protected constructor(
        protected subscription: Subscription = new Subscription(),
        protected itemsSub: Subject<T[]> = new ReplaySubject()
    ) {
        this.selectedSub = new ReplaySubject();
        this.isSuccess = new ReplaySubject();
        this.feedbackMessage = new ReplaySubject();
    }

    ngOnInit(): void {
        // Set up the table items.
        this.setupItems();
        // Reset the form based on selection.
        this.subscription.add(
            this.selectedSub.subscribe(item => {
                if (item) {
                    return this.setupForm(item);
                }
                return this.setupForm();
            })
        );
        this.setupForm();
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    closeModal(modal: ModalComponent): void {
        modal.close();
    }

    prepareAdd(): void {
        this.selectedSub.next(null);
        this.isSuccess.next(null);
        this.modifyModal.open();
    }

    prepareUpdate(item: T): void {
        this.selectedSub.next(item);
        this.isSuccess.next(null);
        this.modifyModal.open();
    }

    prepareDelete(item: T): void {
        this.selectedSub.next(item);
        this.isSuccess.next(null);
        this.deleteModal.open();
    }

    sendBackendRequest(request: Observable<any>, onSuccessMessage?: string): void {
        this.isSuccess.next(undefined);

        request.subscribe(
            () => {
                this.isSuccess.next(true);

                this.setupForm();
                this.setupItems();

                this.deleteModal?.close();
                this.modifyModal?.close();

                if (onSuccessMessage) {
                    this.feedbackMessage.next(onSuccessMessage);
                }
            },
            (error: { message: string; }) => {
                this.isSuccess.next(false);
                this.feedbackMessage.next(error.message);
            }
        );
    }

    getTableActions(): TableAction[] {
        return [
            new EditAction((item: T) => {
                this.prepareUpdate(item);
            }),
            new DeleteAction((item: T) => {
                this.prepareDelete(item);
            }),
        ];
    }

    setupForm(item?: T): void {}

    storeAdd(body: any = this.formGroup.value): void {}

    storeUpdate(item: T, body: any = this.formGroup.value): void {}

    storeDelete(item: T): void {}

    abstract getTableMapper(): TableMapper;

    abstract setupItems(): void;
}
