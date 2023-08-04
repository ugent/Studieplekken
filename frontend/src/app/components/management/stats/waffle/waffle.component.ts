import {AfterViewInit, Component, HostListener, Input, OnInit, ViewEncapsulation} from '@angular/core';
import * as d3 from 'd3';
import {LocationStat} from '../../../../model/LocationStat';
import {TranslateService} from '@ngx-translate/core';
import {v4 as uuid} from 'uuid';

@Component({
    selector: 'app-waffle',
    encapsulation: ViewEncapsulation.None,
    templateUrl: './waffle.component.html',
    styleUrls: ['./waffle.component.scss']
})
export class WaffleComponent implements AfterViewInit {
    @Input()
    locationStat: LocationStat;

    private text: any;
    public uuid: string = 'd' + uuid(); // make sure the uuid does not start with a digit.

    constructor(
        private translate: TranslateService
    ) {
        this.translate.onLangChange.subscribe(() => {
            this.updateGraphTranslations();
        });
    }

    ngAfterViewInit(): void {
        this.makeGraph();
    }

    private makeGraph(): void {
        const dataset = Array.from(Array(100).keys());

        const width = 300;
        const height = 220;

        const takenSeats = (this.locationStat.numberOfTakenSeats / this.locationStat.numberOfSeats) * 100;

        const graph = d3.select('.' + this.uuid).append('svg').attr('width', width).attr('height', height);
        const rectangles = graph
            .selectAll('rect')
            .data(dataset).enter()
            .append('rect');

        rectangles.attr('x', (d, i) => (i % 10) * 20 + 50)
            .attr('y', (d, i) => (Math.floor(i / 10) * 20))
            .attr('width', 15)
            .attr('height', 15)
            .attr('fill', d => {
                if (!this.locationStat.reservable) {
                    return '#009de0';
                }
                if (d >= takenSeats) {
                    return 'lightgrey';
                }
                return '#0071a1';
            });

        this.text = graph
            .append('text')
            .text(this.locationStat.reservable ? `${this.locationStat.numberOfTakenSeats} / ${this.locationStat.numberOfSeats} ${this.translate.instant('management.stats.occupied')}` : this.translate.instant('management.stats.withoutReservation'))
            .attr('x', 150)
            .attr('y', 215)
            .style('text-anchor', 'middle')
            .style('margin-top', '1rem')
            .attr('font-family', 'sans-serif')
            .attr('font-size', '14px')
            .attr('fill', 'black');
    }

    private updateGraphTranslations(): void {
        this.text.text(this.locationStat.reservable ? `${this.locationStat.numberOfTakenSeats} / ${this.locationStat.numberOfSeats} ${this.translate.instant('management.stats.occupied')}` : this.translate.instant('management.stats.withoutReservation'));
    }
}
