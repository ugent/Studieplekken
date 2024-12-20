import {Directive, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {BehaviorSubject, Observable, ReplaySubject, Subject, Subscription} from 'rxjs';
import { FormGroup } from '@angular/forms';
import {TableComponent} from '@/contracts/table.component.interface';
import {DeleteAction, EditAction, TableAction, TableMapper} from '@/model/Table';
import {ModalComponent} from '../stad-gent-components/molecules/modal/modal.component';

@Directive()
export abstract class BaseManagementComponent<T extends object> implements OnInit, OnDestroy, TableComponent<T> {

    @ViewChild('modify') modifyModal: ModalComponent;
    @ViewChild('remove') deleteModal: ModalComponent;

    protected selectedSub$: Subject<T>;
    protected refresh$: Subject<void>;
    protected isSuccess: Subject<boolean>;
    protected feedbackMessage: Subject<string>;

    protected formGroup: FormGroup;

    protected constructor(
        protected subscription: Subscription = new Subscription()
    ) {
        this.refresh$ = new BehaviorSubject(undefined);
        this.selectedSub$ = new BehaviorSubject(undefined);
        this.isSuccess = new BehaviorSubject(undefined);
        this.feedbackMessage = new BehaviorSubject(undefined);
    }

    ngOnInit(): void {
        // Reset the form based on selection.
        this.subscription.add(
            this.selectedSub$.subscribe(item => {
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
        this.refresh$.complete();
    }

    closeModal(modal: ModalComponent): void {
        modal.close();
    }

    prepareAdd(): void {
        this.selectedSub$.next(null);
        this.isSuccess.next(null);
        this.modifyModal.open();
    }

    prepareUpdate(item: T): void {
        this.selectedSub$.next(item);
        this.isSuccess.next(null);
        this.modifyModal.open();
    }

    prepareDelete(item: T): void {
        this.selectedSub$.next(item);
        this.isSuccess.next(null);
        this.deleteModal.open();
    }

    sendBackendRequest(request: Observable<any>, onSuccessMessage?: string): void {
        this.isSuccess.next(undefined);

        request.subscribe(
            () => {
                this.isSuccess.next(true);

                this.setupForm();
                this.refresh$.next();

                this.deleteModal?.close();
                this.modifyModal?.close();

                if (onSuccessMessage) {
                    this.feedbackMessage.next(onSuccessMessage);
                }
            },
            (err: { message: string; }) => {
                this.isSuccess.next(false);
                this.feedbackMessage.next(err.message);
            }
        );
    }

    getTableActions(): TableAction<T>[] {
        return [
            new EditAction((item: T) => {
                this.prepareUpdate(item);
            }),
            new DeleteAction((item: T) => {
                this.prepareDelete(item);
            }),
        ];
    }

    getTableMapper(): TableMapper<T> {
        return (_: T) => ({});
    }

    setupForm(_?: T): void {
        this.formGroup = new FormGroup({});
    }
}
