import {Component, OnInit} from '@angular/core';
import {EnvironmentService} from '../../../services/environment/environment.service';

@Component({
    selector: 'app-footer',
    templateUrl: './footer.component.html',
    styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {

    constructor(
        protected environment: EnvironmentService
    ) {
    }

    ngOnInit(): void {
    }

}
