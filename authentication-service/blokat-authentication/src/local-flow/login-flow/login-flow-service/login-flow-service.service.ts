import { Injectable } from "@nestjs/common";
import { AuthService } from "src/auth/auth.service";

@Injectable()
export class LoginFlowService {
  constructor(private authService: AuthService) {}
  /**
   * This handle validates the body, writes a token and returns it.
   * If it's not valid, throw a good error.
   */
  async handleLogin(body: any): Promise<{ access_token: string }> {
    // TODO get user from database

    // TODO validate password

    // TODO translate user to SamlUser

    // TODO write token using authService

    // TODO return it
    return { access_token: "" };
  }
}
