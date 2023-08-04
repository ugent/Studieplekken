import {Directive, OnDestroy, OnInit, TemplateRef, ViewChild} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { BehaviorSubject, Observable, ReplaySubject, Subject, Subscription } from 'rxjs';
import { FormGroup } from '@angular/forms';
import {HasTableComponent} from '../../../contracts/has-table.component.interface';
import {DeleteAction, EditAction, TableAction, TableMapper} from '../../../model/Table';
import {User} from '../../../model/User';
import {ModalComponent} from '../../stad-gent-components/molecules/modal/modal.component';

@Directive()
export abstract class BaseManagementComponent<T extends object> implements OnInit, OnDestroy, HasTableComponent {

    static reloads = 0;

    static getReloads() {
        return ++this.reloads;
    }

    @ViewChild('modify') modifyModal: ModalComponent;
    @ViewChild('remove') deleteModal: ModalComponent;

    protected userSub: Subject<User>;
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
            this.selectedSub.subscribe(building => {
                if (building) {
                    return this.setupForm(building);
                }
                return this.setupForm();
            })
        );
        this.setupForm();
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    abstract setupForm(item?: T): void;

    abstract setupItems(): void;

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

    storeAdd(body: any): void {}

    storeUpdate(item: T, body: any): void {}

    storeDelete(item: T): void {}

    sendBackendRequest(request: Observable<any>, onSuccessMessage?: string): void {
        this.isSuccess.next(undefined);

        request.subscribe(
            () => {
                this.isSuccess.next(true);

                this.setupForm();
                this.setupItems();

                this.deleteModal.close();
                this.modifyModal.close();

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

    abstract getTableMapper(): TableMapper;
}
