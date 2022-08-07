export interface Token {
  token: string;
  purpose: string;
  email: string;
}

export class TokenConstructor {
  static new(): Token {
    return {
      token: '',
      purpose: '',
      email: null,
    };
  }

  static newFromObj(obj: Token): Token {
    if (obj === null) {
      return null;
    }

    return {
      token: obj.token,
      purpose: obj.purpose,
      email: obj.email,
    };
  }
}
