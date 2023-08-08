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

    static newFromObj(obj: Record<string, any>): Token {
        if (obj === null) {
            return null;
        }

        return {
            token: obj.token || obj.id,
            purpose: obj.purpose,
            email: obj.email,
            isUsed: obj.isUsed,
        };
    }
}
