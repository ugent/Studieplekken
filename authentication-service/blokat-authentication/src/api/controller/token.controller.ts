import { Body, Controller, Get, Post, UseGuards, UsePipes, ValidationPipe } from '@nestjs/common';
import { DbTokenService } from 'src/db/db-token/db-token.service';
import { BackendServiceGuard } from '../internal-auth/service-jwt/backend-service.guard';
import { TokenBody } from './bodies/token.body';

@Controller('auth/tokens')
@UseGuards(BackendServiceGuard)
export class TokenController {
    constructor(private tokenDb: DbTokenService) {}

    @Get()
    async getAllTokens() {
        return {"tokens": await this.tokenDb.allTokens()};
    }

    @Post()
    @UsePipes(new ValidationPipe())
    async createToken(@Body() token: TokenBody) {
        return await this.tokenDb.createToken(token.email, token.purpose);
    }
}
