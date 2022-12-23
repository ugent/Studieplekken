import { CanActivate, ExecutionContext, Injectable, Logger, Request } from '@nestjs/common';
import { verify } from 'jsonwebtoken';
import { ConfigService } from 'src/configModule/config.service';

class JWTValidationError extends Error {}

@Injectable()
export class BackendServiceGuard implements CanActivate {
    constructor(private config: ConfigService) {

    }

    canActivate(context: ExecutionContext): boolean | Promise<boolean> {
        const request: Request = context.switchToHttp().getRequest();
        // @ts-ignore
        const jwtHeader = request.headers['authorization'] || "";
        
        return this.validateBackendHeader(jwtHeader);    
    }

    validateBackendHeader(header: string): boolean {
        try {
            const jwt = this.extractBearerToken(header);
            this.checkJWT(jwt);
            return true;
        } catch (e) {
            if(!(e instanceof JWTValidationError)) throw e;
    
            Logger.error(`Invalid JWT token used on a service backend. Error: ${e.message}`)
            return false;
        }
    }

    private extractBearerToken(header: string): string {
        const validatorRegex = /^[Bb]earer ([^ ]*)$/
        const match = header.match(validatorRegex)
        if (!match || match.length < 2)
            throw new JWTValidationError(`Cannot extract valid bearer token. Header: ${header}`)
        
        return match[1]
    }

    private checkJWT(jwt: string) {
        try {
            return verify(jwt, this.config.getCurrentConfiguration().backendServiceJwtKey)
        } catch(e) {
            throw new JWTValidationError(`JWT validation failed with error: ${e.name} ${e.message}`);
        }
    }
}


