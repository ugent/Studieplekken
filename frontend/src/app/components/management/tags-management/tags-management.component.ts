import {ChangeDetectionStrategy, Component} from '@angular/core';
import {TagsService} from '@/services/api/tags/tags.service';
import {LocationTag, LocationTagConstructor} from '@/model/LocationTag';
import {TableMapper} from '@/model/Table';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {BaseManagementComponent} from '../base-management.component';
import {EMPTY, Observable} from 'rxjs';
import {startWith, switchMap} from 'rxjs/operators';

@Component({
    selector: 'app-tags-management',
    templateUrl: './tags-management.component.html',
    styleUrls: ['./tags-management.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TagsManagementComponent extends BaseManagementComponent<LocationTag> {

    protected tagsObs$: Observable<LocationTag[]>;

    constructor(
        private tagsService: TagsService
    ) {
        super();
    }

    ngOnInit(): void {
        super.ngOnInit();

        this.tagsObs$ = this.refresh$.pipe(
            startWith(EMPTY), switchMap(() =>
                this.tagsService.getAllTags()
            )
        );
    }

    setupForm(tag: LocationTag = LocationTagConstructor.new()): void {
        this.formGroup = new FormGroup({
            tagId: new FormControl(tag.tagId),
            dutch: new FormControl(tag.dutch, Validators.required),
            english: new FormControl(tag.english, Validators.required)
        });
    }

    storeAdd(body = this.formGroup.value): void {
        this.sendBackendRequest(
            this.tagsService.addTag(body)
        );
    }

    storeUpdate(tag: LocationTag, body = this.formGroup.value): void {
        this.sendBackendRequest(
            this.tagsService.updateTag({
                ...tag,
                ...body
            })
        );
    }

    storeDelete(tag: LocationTag): void {
        this.sendBackendRequest(
            this.tagsService.deleteTag(tag)
        );
    }

    getTableMapper(): TableMapper<LocationTag> {
        return (tag: LocationTag) => ({
            'management.tags.table.dutch': tag.dutch,
            'management.tags.table.english': tag.english
        });
    }
}
