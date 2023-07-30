import {AfterViewInit, Component, ElementRef, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Location} from '../../../../extensions/model/Location';
import * as Leaf from 'leaflet';

// Leaflet stuff.
const iconRetinaUrl = './assets/marker-icon-2x.png';
const iconUrl = './assets/marker-icon.png';
const shadowUrl = './assets/marker-shadow.png';
const iconDefault = Leaf.icon({
    iconRetinaUrl,
    iconUrl,
    shadowUrl,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    tooltipAnchor: [16, -28],
    shadowSize: [41, 41]
});
Leaf.Marker.prototype.options.icon = iconDefault;

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
