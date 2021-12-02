import { Injectable } from "@nestjs/common";
import * as crypto from "crypto";

@Injectable()
export class HashedService {
  async firstHash(pw: string) {
    const salt = crypto.randomBytes(64).toString("hex");
    return {
      hash: await this.hash(pw, salt),
      salt: salt,
    };
  }

  async hash(pw: string, salt: string): Promise<string> {
    const hash = await this.scrypt(pw, salt, 64);
    return hash;
  }

  async scrypt(
    password: string,
    salt: string,
    keylength: number,
  ): Promise<string> {
    return new Promise((resolve, reject) => {
      return crypto.scrypt(password, salt, keylength, (err, dk) => {
        if (err) return reject(err);
        else return resolve(dk.toString("hex"));
      });
    });
  }
}
