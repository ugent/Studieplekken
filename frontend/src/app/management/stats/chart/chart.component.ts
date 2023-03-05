import {AfterViewInit, Component, ElementRef, Input, ViewChild} from '@angular/core';
import {Chart} from "chart.js/auto";

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
        let color = '#b2d4e2';
        let datasets = {};

        for (let hois of Object.values(this.reservations)) {
            for (let [hoi, reservations] of Object.entries(hois)) {
                if (!datasets[hoi]) {
                    datasets[hoi] = { label: hoi, data: [], backgroundColor: color };
                    color = this.decreaseTint(color, 25);
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

    /**
     * Decrease a hex color in brightness.
     *
     * @param hex
     * @param amount
     * @private
     */
    private decreaseTint(hex: string, amount: number): string {
        // parse the hex color string to RGB values
        let r = parseInt(hex.slice(1, 3), 16);
        let g = parseInt(hex.slice(3, 5), 16);
        let b = parseInt(hex.slice(5, 7), 16);

        // decrease the RGB values by the specified amount
        r = Math.max(0, r - amount);
        g = Math.max(0, g - amount);
        b = Math.max(0, b - amount);

        // convert the RGB values back to hex color string
        return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
    }
}
