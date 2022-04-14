import { AfterViewInit, Component, HostListener, Input, OnInit, ViewEncapsulation } from '@angular/core';
import * as d3 from 'd3';
import { LocationStat } from '../../../shared/model/LocationStat';
import { TranslateService } from '@ngx-translate/core';
import { v4 as uuid } from 'uuid';

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

    const width = 180;
    const height = 180;

    const takenSeats = (this.locationStat.numberOfTakenSeats / this.locationStat.numberOfSeats) * 100;

    const div = d3.select('.' + this.uuid).append('div')
      .attr('class', 'tooltip')
      .style('opacity', 0);

    const graph = d3.select('.' + this.uuid).append('svg').attr('width', width).attr('height', height);
    const rectangles = graph
      .selectAll('rect')
      .data(dataset).enter()
      .append('rect');

    rectangles.attr('x', (d, i) => (i % 10) * 15 + 15)
      .attr('y', (d, i) => (Math.floor(i / 10) * 15))
      .attr('width', 14)
      .attr('height', 14)
      .attr('fill', d => {
        if (!this.locationStat.reservable) {
          return '#009de0';
        }
        if (d >= takenSeats) {
          return 'grey';
        }
        if (takenSeats < 80) {
          return 'green';
        }
        if (takenSeats < 95) {
          return 'orange';
        }
        return 'red';
      })
      .on('mouseover', (event, d) => {
        div.transition()
          .duration(200)
          .style('opacity', .9);
        div.html(this.locationStat.reservable ? (d < takenSeats ? this.translate.instant('management.stats.takenSeats')
          : this.translate.instant('management.stats.freeSeats')) : this.translate.instant('management.stats.unknown'))
          .style('left', (event.pageX) + 'px')
          .style('top', (event.pageY - 28) + 'px');
      })
      .on('mouseout', (event, d) => {
        div.transition()
          .duration(500)
          .style('opacity', 0);
      });

    this.text = graph
      .append('text')
      .text(this.locationStat.reservable ? `${this.locationStat.numberOfTakenSeats} / ${this.locationStat.numberOfSeats} ${this.translate.instant('management.stats.occupied')}` : this.translate.instant('management.stats.withoutReservation'))
      .attr('x', 90)
      .attr('y', 170)
      .style('text-anchor', 'middle')
      .attr('font-family', 'sans-serif')
      .attr('font-size', '14px')
      .attr('fill', 'black');
  }

  private updateGraphTranslations(): void {
    this.text.text(this.locationStat.reservable ? `${this.locationStat.numberOfTakenSeats} / ${this.locationStat.numberOfSeats} ${this.translate.instant('management.stats.occupied')}` : this.translate.instant('management.stats.withoutReservation'));
  }
}
