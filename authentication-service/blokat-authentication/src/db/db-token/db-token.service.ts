import { Injectable } from "@nestjs/common";
import { tokens } from "@prisma/client";
import { DbService } from "../db.service";

@Injectable()
export class DbTokenService {
  constructor(private prisma: DbService) {}

  async checkToken(tokenId: string, purpose: string) {
    const token = await this.prisma.tokens.findUnique({
      where: { id: tokenId },
    });
    if (!token) throw new TokenDoesntExistError();

    if (token.purpose !== purpose) {
      throw new TokenDoesntExistError();
    }
    if (token.isUsed) {
      throw new TokenIsUsedError();
    }

    return token;
  }

  async useToken(tokenId: string, purpose: string): Promise<tokens> {
    const token = await this.prisma.tokens.findUnique({
      where: { id: tokenId },
    });
    if (!token) throw new TokenDoesntExistError();

    if (token.purpose !== purpose) {
      throw new TokenDoesntExistError();
    }
    if (token.isUsed) {
      throw new TokenIsUsedError();
    }

    token.isUsed = true;
    return await this.prisma.tokens.update({
      where: { id: token.id },
      data: { isUsed: true, email: token.email },
    });
  }

  async allTokens(): Promise<tokens[]> {
    return this.prisma.tokens.findMany();
  }

  async createToken(email: string, purpose: string): Promise<tokens> {
    return this.prisma.tokens.create({data: {email, purpose, createdAt: new Date()}})
  }
}

class TokenIsUsedError extends Error {
  constructor() {
    super("The token has already been used.");
  }
}

class TokenDoesntExistError extends Error {
  constructor() {
    super("The token is invalid.");
  }
}
