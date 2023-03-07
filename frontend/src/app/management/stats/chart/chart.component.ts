import {AfterViewInit, Component, ElementRef, Input, ViewChild} from '@angular/core';
import {Chart} from "chart.js/auto";
import {HOIColors} from "../../../app.constants";

@Component({
    selector: 'app-chart',
    templateUrl: './chart.component.html',
    styleUrls: ['./chart.component.scss']
})
export class ChartComponent implements AfterViewInit {
    /**
     * Reservations per HOI, grouped by date.
     */
    @Input('reservations') private reservations: Map<string, Map<string, number>>

    /**
     * The template chart element.
     */
    @ViewChild('chart') private chartElement: ElementRef;

    /**
     * The chart.js data container.
     */
    private chart: Chart

    /**
     * We use this hook to make sure the canvas was loaded.
     */
    ngAfterViewInit(): void {
        let datasets = {};

        for (let hois of Object.values(this.reservations)) {
            for (let [hoi, reservations] of Object.entries(hois)) {
                if (!datasets[hoi]) {
                    datasets[hoi] = { label: hoi, data: [], backgroundColor: HOIColors[hoi] ?? '#1E64C8' };
                }
                datasets[hoi].data.push(reservations);
            }
        }

        this.chart = new Chart(this.chartElement.nativeElement, {
            type: 'bar',
            data: {
                labels: Object.keys(this.reservations),
                datasets: Object.values(datasets)
            },
            options: {
                scales: {
                    x: {
                        stacked: true
                    },
                    y: {
                        stacked: true
                    }
                }
            }
        });
    }
}
