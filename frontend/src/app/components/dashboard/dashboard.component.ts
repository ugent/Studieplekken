import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractControl, UntypedFormControl, UntypedFormGroup} from '@angular/forms';
import {MatSelectChange} from '@angular/material/select';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {Moment} from 'moment';
import {merge, Observable, of, Subscription} from 'rxjs';
import {map} from 'rxjs/operators';
import {environment} from 'src/environments/environment';
import {LocationStatus} from '@/app.constants';
import {BuildingService} from '@/services/api/buildings/buildings.service';
import {LocationService} from '@/services/api/locations/location.service';
import {TagsService} from '@/services/api/tags/tags.service';
import {Building} from '@/model/Building';
import {Location} from '@/model/Location';
import {LocationTag} from '@/model/LocationTag';
import {BreadcrumbService} from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit, OnDestroy {
    public locations: Location[];
    public locationNextReservableFroms = new Map<string, Moment>();
    public filteredLocations: Location[] = [];
    public filteredLocationsBackup: Location[] = [];

    public selectedTags: LocationTag[] = [];
    public selectedBuilding: Building;
    public locationSearch: string;

    public tagFilterFormGroup = new UntypedFormGroup({
        filteredTags: new UntypedFormControl(''),
    });

    public buildingFilterFormGroup = new UntypedFormGroup({
        filteredBuilding: new UntypedFormControl(''),
    });

    public currentLang: string;
    public successOnRetrievingLocations: boolean = undefined;
    public showStagingWarning = environment.showStagingWarning;
    public showOpen = false;

    private locationSub: Subscription;
    private nextReservableFromSub: Subscription;

    public buildingObs: Observable<Building[]>;
    public tagObs: Observable<LocationTag[]>;

    constructor(
        private locationService: LocationService,
        private tagsService: TagsService,
        private translate: TranslateService,
        private breadcrumbService: BreadcrumbService,
        private buildingService: BuildingService
    ) {
    }

    public ngOnInit(): void {
        this.currentLang = this.translate.currentLang;
        this.successOnRetrievingLocations = null;

        this.translate.onLangChange.subscribe(() => {
            this.currentLang = this.translate.currentLang;
        });

        this.locationSub = this.locationService.getLocations().subscribe(
            (next) => {
                this.locations = next;
                this.filteredLocations = next;
                this.filteredLocationsBackup = next;
                this.successOnRetrievingLocations = true;
            },
            (err) => {
                console.error(err);
                this.successOnRetrievingLocations = false;
            }
        );

        this.nextReservableFromSub = this.locationService.getAllLocationNextReservableFroms()
            .subscribe((next) => {
                next.forEach((next2) => {
                    this.locationNextReservableFroms.set(next2.locationName, next2.nextReservableFrom);
                });
            });

        this.tagObs = this.tagsService.getAllTags();
        this.buildingObs = this.buildingService.getAllBuildings();

        this.breadcrumbService.setCurrentBreadcrumbs([])
    }

    ngOnDestroy(): void {
        if (this.locationSub) {
            this.locationSub.unsubscribe();
        }
        if (this.nextReservableFromSub) {
            this.nextReservableFromSub.unsubscribe();
        }
    }

    /**
     * Used as a compareWith input on the tags-selection field in the filter
     * Tracks identities when checking for changes
     */
    compareTagsInSelection(tag1: LocationTag, tag2: LocationTag): boolean {
        return !tag1 || !tag2 ? false : tag1.tagId === tag2.tagId;
    }

    /**
     * When the selection of tags to filter on is changed
     */
    onTagsSelectionChange(event: MatSelectChange): void {
        this.selectedTags = event.value as LocationTag[];
        this.displayFilterLocations();
    }

    /**
     * When the selection of building to filter on is changed
     */
    onBuildingSelectionChange(event: MatSelectChange): void {
        this.selectedBuilding = event.value as Building;
        this.displayFilterLocations();
    }

    toggleShowOpen(): void {
        this.showOpen = !this.showOpen;
        this.displayFilterLocations();
    }

    /**
     * This will take into account:
     * - selectedTags
     * - locationSearch
     * - showOpen
     *
     * And filter only those locations that apply to all filters above
     */
    displayFilterLocations(): void {
        this.filteredLocations = [];

        this.locations.forEach((location) => {
            // first check that the location name matches with the search bar
            if (
                this.locationSearch !== undefined &&
                !location.name.toLowerCase().includes(this.locationSearch.toLowerCase())
            ) {
                return;
            }

            // check that the location is in the selected building
            if (
                this.selectedBuilding !== undefined &&
                location.building.buildingId !== this.selectedBuilding.buildingId
            ) {
                return;
            }

            // only filter on tags when there is at least one selected
            if (!(this.selectedTags.length === 0)) {
                // only add when all the tags match
                for (const tag of this.selectedTags) {
                    // if the filtered tag is not assigned to a certain location ...
                    if (!location.assignedTags.some((t) => t.tagId === tag.tagId)) {
                        return;
                    }
                }
            }

            if (this.showOpen) {
                if (location.status.first === LocationStatus.OPEN) {
                    this.filteredLocations.push(location);
                }
            } else {
                this.filteredLocations.push(location);
            }
        });
    }

    onSearchEnter(): void {
        this.displayFilterLocations();
    }

    onClearSearch(): void {
        this.filteredLocations = this.locations;
        this.selectedBuilding = undefined;
        this.selectedTags = [];
        this.filteredTags.setValue([]);
        this.filteredBuilding.setValue('');
        this.locationSearch = '';
        this.showOpen = false;
        this.displayFilterLocations();
    }

    get filteredTags(): AbstractControl {
        return this.tagFilterFormGroup.get('filteredTags');
    }

    get filteredBuilding(): AbstractControl {
        return this.buildingFilterFormGroup.get('filteredBuilding');
    }

    currentLanguage(): Observable<string> {
        return merge<LangChangeEvent, LangChangeEvent>(
            of<LangChangeEvent>({
                lang: this.translate.currentLang,
            } as LangChangeEvent),
            this.translate.onLangChange
        ).pipe(map((s) => s.lang));
    }
}
