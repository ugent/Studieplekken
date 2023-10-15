import {Component} from '@angular/core';
import {EMPTY, Observable, of} from 'rxjs';
import {TokensService} from '../../../../extensions/services/api/tokens/tokens.service';
import {Token} from '../../../../model/Token';
import {
    FormControl,
    FormGroup,
    Validators
} from '@angular/forms';
import {BaseManagementComponent} from '../base-management.component';
import {startWith, switchMap, tap} from 'rxjs/operators';
import {TableAction, TableMapper} from '../../../../model/Table';

@Component({
    selector: 'app-tokens-management',
    templateUrl: './tokens.component.html',
    styleUrls: ['./tokens.component.scss'],
})
export class TokensComponent extends BaseManagementComponent<Token> {

    protected tokensObs$: Observable<Token[]>;

    protected addedToken: Token;
    protected formGroup: FormGroup;

    constructor(
        private tokensService: TokensService
    ) {
        super();
    }

    ngOnInit(): void {
        super.ngOnInit();

        this.tokensObs$ = this.refresh$.pipe(
            startWith(EMPTY), switchMap(() =>
                this.tokensService.getTokens()
            )
        );
    }

    setupForm(_?: Token): void {
        this.formGroup = new FormGroup({
            purpose: new FormControl('', Validators.required.bind(this)),
            email: new FormControl('')
        });
    }

    storeAdd(): void {
        this.sendBackendRequest(
            this.tokensService.addToken(this.formGroup.value).pipe(
                tap(token => this.addedToken = token)
            )
        );
    }

    getTableActions(): TableAction<Token>[] {
        return [];
    }

    getTableMapper(): TableMapper<Token> {
        return (token: Token) => ({
            'management.tokens.table.token': token.token,
            'management.tokens.table.purpose': token.purpose,
            'management.tokens.table.email': token.email,
            'management.tokens.table.used': token.isUsed
        });
    }

    validTokenFormGroup(): boolean {
        const value = this.formGroup.value;

        if (this.formGroup.invalid) {
            return false;
        }

        if (this.formGroup.value.purpose === 'PASSWORD_RESET') {
            return value.email.length > 0 && value.email.includes('@');
        } else {
            return true;
        }
    }
}
