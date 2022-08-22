export interface Token {
  token: string;
  purpose: string;
  email: string;
  isUsed: boolean;
}

export class TokenConstructor {
  static new(): Token {
    return {
      token: '',
      purpose: '',
      email: null,
      isUsed: false,
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
      isUsed: obj.isUsed,
    };
  }
}
