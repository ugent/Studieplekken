export interface Authority {
  authorityId: number;
  name: string;
  description: string;
}

export class AuthorityConstructor {
  static new(): Authority {
    return {
      authorityId: 0,
      name: '',
      description: ''
    };
  }

  static newFromObj(obj: Authority): Authority {
    return {
      authorityId: obj.authorityId,
      name: obj.name,
      description: obj.description
    };
  }
}
