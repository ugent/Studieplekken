import { Injectable } from "@nestjs/common";
import { tokens } from "@prisma/client";
import { DbService } from "../db.service";

@Injectable()
export class DbTokenService {
  constructor(private prisma: DbService) {}

  async createNewToken(): Promise<tokens> {
    return await this.prisma.tokens.create({
      data: {},
    });
  }

  async useToken(tokenId: string): Promise<tokens> {
    const token = await this.prisma.tokens.findUnique({
      where: { id: tokenId },
    });
    if (!token) throw new TokenDoesntExistError();

    if (token.isUsed) {
      throw new TokenIsUsedError();
    }

    token.isUsed = true;
    const updatedToken = await this.prisma.tokens.update({
      where: { id: token.id },
      data: { isUsed: true },
    });

    return updatedToken;
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
