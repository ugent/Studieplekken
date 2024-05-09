import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { api } from '../endpoints';
import { map } from 'rxjs/internal/operators/map';
import {Token, TokenConstructor} from '@/model/Token';

@Injectable({
  providedIn: 'root',
})
export class TokensService {
  constructor(private http: HttpClient) {
  }

  getTokens(): Observable<Token[]> {
    return this.http.get<{tokens: Token[]}>(api.tokens).pipe(map(x => x.tokens.map(TokenConstructor.newFromObj)));
  }

  /**
   * Adding a token. Note that token. token will be ignored. The return
   * value will have set the correct token for the added Tag.
   */
  addToken(token: Token): Observable<Token> {
    return this.http.post<Token>(api.tokens, token).pipe(map(TokenConstructor.newFromObj));
  }

}
