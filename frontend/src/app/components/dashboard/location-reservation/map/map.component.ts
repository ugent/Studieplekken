import {AfterViewInit, Component, ElementRef, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Location} from '../../../../extensions/model/Location';
import * as Leaf from 'leaflet';

@Component({
    selector: 'app-map',
    templateUrl: './map.component.html',
    styleUrls: ['./map.component.scss']
})
export class MapComponent implements AfterViewInit, OnDestroy {

    @Input()
    protected location: Location;

    @ViewChild('leafletContainer')
    leafletContainer: ElementRef<HTMLDivElement>;

    private leafletMap: Leaf.Map;

    constructor() {
    }

    ngAfterViewInit(): void {
        this.leafletMap?.off();
        this.leafletMap?.remove();

        const originalTile = Leaf.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19
        });

        const coordinates = new Leaf.LatLng(
            this.location.building.latitude, this.location.building.longitude
        );

        this.leafletMap = new Leaf.Map(this.leafletContainer.nativeElement, {
            center: coordinates,
            zoom: 18,
            layers: [originalTile],
            crs: Leaf.CRS.EPSG3857
        });

        new Leaf.Marker(coordinates).addTo(
            this.leafletMap
        );
    }

    ngOnDestroy(): void {
        this.leafletMap?.off();
        this.leafletMap?.remove();
    }
}
