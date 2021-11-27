import {
  Body,
  Controller,
  Get,
  Logger,
  Post,
  Query,
  Render,
  Res,
  ValidationPipe,
} from "@nestjs/common";
import { Response } from "express";
import { getConfig } from "src/configModule/config.service";
import { LoginFlowService } from "./login-flow/login-flow-service/login-flow-service.service";
import { RegisterFlowService } from "./register-flow/register-flow-service";
import { UnhashedRegisterBodyBase } from "./register-flow/RegisterBodyInterface";

@Controller("/auth/local")
export class LocalFlowController {
  constructor(
    private registerFlow: RegisterFlowService,
    private loginFlow: LoginFlowService,
  ) {}

  @Post("register")
  registerNewAccount(@Body(ValidationPipe) body: UnhashedRegisterBodyBase) {
    return this.registerFlow.handleRegistration(body);
  }

  @Get("register")
  @Render("register")
  getRegisterPage(@Query("token") token: string) {
    return { token };
  }

  @Get("login")
  @Render("login")
  getLoginPage(@Query("callbackURL") callbackURL: string) {
    return { callbackURL };
  }

  @Post("login")
  async login(
    @Query("callbackURL") callbackURL: string,
    // TODO: we should make a type that validates the incoming response, see 'registerNewAccount' function with UnhashedRegisterBody
    @Body(ValidationPipe) body: any,
    @Res() res: Response,
  ) {
    console.log("enters login page");
    try {
      const token = await this.loginFlow.handleLogin(body);
      console.log(callbackURL);
      if (callbackURL) {
        const configuration = getConfig();
        const allowedCallbacks = configuration.auth.allowedClientCallbacks;

        if (allowedCallbacks.indexOf(callbackURL) !== -1) {
          console.log(`${callbackURL}?token=${token.access_token}`);
          return res.redirect(`${callbackURL}?token=${token.access_token}`);
        } else {
          Logger.warn(`Callback URL ${callbackURL} is not allowed.`);
          res.status(400).send("The URL is not allowed.");
        }
      }
    } catch (e: unknown) {
      // TODO: show error when password is incorrect
      res.render("login", { error: "valuable error", callbackURL });
    }
  }

  @Post("tokenLink")
  async getNewTokenLink() {
    const token = await this.registerFlow.newToken();
    return { token };
  }
}
