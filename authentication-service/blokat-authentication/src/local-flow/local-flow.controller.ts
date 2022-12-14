import {
  Body,
  Controller,
  Get,
  Logger,
  Post,
  Query,
  Render,
  Res,
} from "@nestjs/common";
import { Response } from "express";
import { getConfig } from "src/configModule/config.service";
import { LoginFlowService } from "./login-flow/login-flow-service/login-flow-service.service";
import { UnhashedLoginBodyBase } from "./login-flow/login-flow-service/LoginBodyInterface";
import { RegisterFlowService } from "./register-flow/register-flow-service";
import { UnhashedRegisterBodyBase } from "./register-flow/RegisterBodyInterface";
import { UnhashedPasswordResetBodyBase } from "./password-reset-flow/PasswordResetBodyInterface";
import { PasswordResetFlowService } from "./password-reset-flow/password-reset-flow-service";

@Controller("/auth/local")
export class LocalFlowController {
  constructor(
    private registerFlow: RegisterFlowService,
    private loginFlow: LoginFlowService,
    private passwordResetFlow: PasswordResetFlowService,
  ) {}

  @Post("register")
  async registerNewAccount(
    @Query("token") token: string,
    @Body() body: UnhashedRegisterBodyBase,
    @Res() res: Response,
  ) {
    try {
      const return_val = await this.registerFlow.handleRegistration(body);

      if ("errors" in return_val && return_val["errors"].length != 0) {
        return res.status(400).render("register", {
          errors: return_val["errors"].join(" "),
          first_name: body.first_name,
          email: body.email,
          last_name: body.last_name,
          token,
        });
      }

      res.redirect("https://bloklocaties.stad.gent/login");
    } catch (e: unknown) {
      res.render("register", { errors: "Unknown error", token });
    }
  }

  @Get("register")
  @Render("register")
  getRegisterPage(@Query("token") token: string) {
    return { token: token };
  }

  @Get("login")
  @Render("login")
  getLoginPage(@Query("callbackURL") callbackURL: string) {
    return { callbackURL };
  }

  @Post("login")
  async login(
    @Query("callbackURL") callbackURL: string,
    @Body() body: UnhashedLoginBodyBase,
    @Res() res: Response,
  ) {
    try {
      const response = await this.loginFlow.handleLogin(body);

      if (response.errors.length > 0) {
        res.render("login", {
          errors: response.errors.join(" "),
          email: body.email,
        });
      } else {
        if (callbackURL) {
          const configuration = getConfig();
          const allowedCallbacks = configuration.auth.allowedClientCallbacks;

          if (allowedCallbacks.indexOf(callbackURL) !== -1) {
            return res.redirect(
              `${callbackURL}?token=${response.access_token}`,
            );
          } else {
            Logger.warn(`Callback URL ${callbackURL} is not allowed.`);
            return res.status(400).send("The URL is not allowed.");
          }
        } else {
          return res.status(400).send("No callbackURL.");
        }
      }
    } catch (e: unknown) {
      Logger.error(e);
      res.render("login", { errors: "valuable error", callbackURL });
    }
  }

  @Get("password-reset")
  @Render("password-reset")
  getPasswordResetPage(@Query("token") token: string) {
    return { token: token };
  }

  @Post("password-reset")
  async passwordReset(
    @Query("token") token: string,
    @Body() body: UnhashedPasswordResetBodyBase,
    @Res() res: Response,
  ) {
    try {
      const response = await this.passwordResetFlow.handlePasswordReset(body);

      if (response.errors.length > 0) {
        res.render("password-reset", {
          errors: response.errors.join(" "),
          token,
        });
      } else {
        res.redirect("https://bloklocaties.stad.gent/login");
      }
    } catch (e) {
      res.render("password-reset", { errors: "Unknown error", token });
    }
  }
}
