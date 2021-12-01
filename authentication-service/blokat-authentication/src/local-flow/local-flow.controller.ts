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
  Redirect,
} from '@nestjs/common';
import { Response } from 'express';
import { getConfig } from 'src/configModule/config.service';
import { LoginFlowService } from './login-flow/login-flow-service/login-flow-service.service';
import { UnhashedLoginBodyBase } from './login-flow/login-flow-service/LoginBodyInterface';
import { RegisterFlowService } from './register-flow/register-flow-service';
import { UnhashedRegisterBodyBase } from './register-flow/RegisterBodyInterface';

@Controller('/auth/local')
export class LocalFlowController {
  constructor(
    private registerFlow: RegisterFlowService,
    private loginFlow: LoginFlowService,
  ) {}

  @Post('register')
  async registerNewAccount(
    @Query('callbackURL') callbackURL: string,
    @Query('token') token: string,
    @Body() body: UnhashedRegisterBodyBase,
    @Res() res: Response,
  ) {
    try {
      const return_val = await this.registerFlow.handleRegistration(body);

      if ('errors' in return_val && return_val['errors'].length != 0) {
        res.render('register', {
          error: return_val['errors'],
          first_name: body.first_name,
          email: body.email,
          last_name: body.last_name,
        });
      }
      if (callbackURL) {
        const configuration = getConfig();
        const allowedCallbacks = configuration.auth.allowedClientCallbacks;

        if (
          allowedCallbacks.indexOf(callbackURL) !== -1 &&
          !('errors' in return_val)
        ) {
          return res.redirect(`${callbackURL}`);
        } else if (!('errors' in return_val)) {
          Logger.warn(`Callback URL ${callbackURL} is not allowed.`);
          res.status(400).send('The URL is not allowed.');
        }
      }
    } catch (e: unknown) {
      res.render('register', { error: 'valuable error' });
    }
  }

  @Get('register')
  @Render('register')
  getRegisterPage(
    @Query('callbackURL') callbackURL: string,
    @Query('token') token: string,
  ) {
    return { callbackURL: callbackURL, token: token };
  }

  @Get('login')
  @Render('login')
  getLoginPage(@Query('callbackURL') callbackURL: string) {
    return { callbackURL };
  }

  @Post('login')
  async login(
    @Query('callbackURL') callbackURL: string,
    @Body() body: UnhashedLoginBodyBase,
    @Res() res: Response,
  ) {
    console.log('enters login page');
    try {
      const response = await this.loginFlow.handleLogin(body);
      if (response.errors.length > 0) {
        res.render('login', { error: response.errors, email: body.email });
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
            res.status(400).send('The URL is not allowed.');
          }
        }
      }
    } catch (e: unknown) {
      res.render('login', { error: 'valuable error', callbackURL });
    }
  }

  @Post('tokenLink')
  async getNewTokenLink() {
    const token = await this.registerFlow.newToken();
    return { token };
  }
}
