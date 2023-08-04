export interface Authority {
  authorityId: number;
  authorityName: string;
  description: string;
}

export class AuthorityConstructor {
  static new(): Authority {
    return {
      authorityId: 0,
      authorityName: '',
      description: '',
    };
  }

  static newFromObj(obj: Authority): Authority {
    return {
      authorityId: obj.authorityId,
      authorityName: obj.authorityName,
      description: obj.description,
    };
  }
}
