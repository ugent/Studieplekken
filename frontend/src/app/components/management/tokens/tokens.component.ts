import { Component, OnInit, TemplateRef} from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import {TokensService} from '../../../extensions/services/api/tokens/tokens.service';
import {Token} from '../../../extensions/model/Token';
import {AbstractControl, UntypedFormControl, UntypedFormGroup, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';

@Component({
  selector: 'app-tokens',
  templateUrl: './tokens.component.html',
  styleUrls: ['./tokens.component.scss'],
})
export class TokensComponent implements OnInit {
  loading = true;
  tokensObs: Observable<Token[]>;

  currentSort = 'domain';
  sortOrder: 'asc' | 'des'  = 'asc';
  orderMarkers = {
    token: 'sort-both',
    purpose: 'sort-both',
    email: 'sort-both',
    isUsed: 'sort-both'
  };

  tokenFormGroup = new UntypedFormGroup({
    purpose: new UntypedFormControl('', Validators.required.bind(this)),
    email: new UntypedFormControl(''),
  });

  successAddingToken: boolean = undefined;

  createdToken: Token = undefined;

  errorOnRetrievingTokens = false; // booleanId = 0

  constructor(
    private tokensService: TokensService,
    private modalService: MatDialog
  ) {
  }

  get purpose(): AbstractControl {
    return this.tokenFormGroup.get('purpose');
  }

  get email(): AbstractControl {
    return this.tokenFormGroup.get('email');
  }

  get token(): Token {
    return {
      token: null,
      purpose: this.purpose.value as string,
      email: this.email.value as string,
      isUsed: false,
    };
  }

  ngOnInit(): void {
    this.tokensObs = this.tokensService.getTokens().pipe(
      tap(() => (this.loading = false)),
      catchError((e) => {
        this.errorOnRetrievingTokens = !!e;
        return of<Token[]>([]);
      })
    );
  }

  sortOn(val: string): void {
    this.orderMarkers[this.currentSort] = 'sort-both';
    if (this.currentSort === val) {
      if (this.sortOrder === 'asc') {
        this.sortOrder = 'des';
        this.orderMarkers[val] = 'sort-up';
      } else {
        this.sortOrder = 'asc';
        this.orderMarkers[val] = 'sort-down';
      }

    } else {
      this.sortOrder = 'des';
      this.orderMarkers[val] = 'sort-up';
    }
    this.currentSort = val;
  }

  doSort(tokens: Token[]): Token[] {
    const orderFactor = this.sortOrder === 'asc' ? 1 : -1;
    if (this.currentSort === 'token') {
      return tokens.sort((a, b) => {
        if (a.token > b.token) {
          return (-1) * orderFactor;
        }
        if (a.token < b.token) {
          return 1 * orderFactor;
        }
        return 0;
      });
    }
    if (this.currentSort === 'purpose') {
      return tokens.sort((a, b ) => {
        if (a.purpose > b.purpose) {
          return (-1) * orderFactor;
        }
        if (a.purpose < b.purpose) {
          return 1 * orderFactor;
        }
        return 0;
      });
    }
    if (this.currentSort === 'email') {
      return tokens.sort((a, b) => {
        if (a.email > b.email) {
          return (-1) * orderFactor;
        }
        if (a.email < b.email) {
          return 1 * orderFactor;
        }
        return 0;
      });
    }
    if (this.currentSort === 'isUsed') {
      return tokens.sort((a, b) => {
        if (a.isUsed > b.isUsed) {
          return (-1) * orderFactor;
        }
        if (a.isUsed < b.isUsed) {
          return 1 * orderFactor;
        }
        return 0;
      });
    }
    return tokens;
  }

  closeModal(): void {
    this.modalService.closeAll();
  }

  prepareAdd(template: TemplateRef<unknown>): void {
    // reset the feedback boolean
    this.successAddingToken = undefined;

    this.tokenFormGroup.setValue({
      purpose: '',
      email: '',
    });

    this.modalService.open(template, {panelClass: ['cs--cyan', 'bigmodal']});
  }

  addToken(): void {
    this.successAddingToken = null;
    this.tokensService.addToken(this.token).subscribe(
      (token) => {
        this.createdToken = token;
        this.successAddingToken = true;
        // and reload the tags
        this.tokensObs = this.tokensService.getTokens();
        this.modalService.closeAll();
      },
      () => {
        this.successAddingToken = false;
      }
    );
  }

  validTokenFormGroup(): boolean {
    if (this.tokenFormGroup.invalid) {
      return false;
    }
    if (this.purpose.value === 'PASSWORD_RESET') {
      return this.email.value.length > 0 && this.email.value.includes('@');
    } else {
      return true;
    }
  }
}
