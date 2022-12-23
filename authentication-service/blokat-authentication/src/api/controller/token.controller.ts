import { Body, Controller, Get, Post, UsePipes, ValidationPipe } from '@nestjs/common';
import { tokens } from '@prisma/client';
import { DbTokenService } from 'src/db/db-token/db-token.service';

@Controller('api/token')
export class TokenController {
    constructor(private tokenDb: DbTokenService) {}

    @Get()
    async getAllTokens() {
        return await this.tokenDb.allTokens();
    }

    @Post()
    @UsePipes(new ValidationPipe())
    async createToken(@Body() token: tokens) {
        this.tokenDb.createToken(token.email, token.purpose);
    }
}
