import { Injectable, Logger } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { Strategy } from 'passport-cas';
import { Institution, SamlUser } from 'src/configModule/config';
import { ConfigService } from 'src/configModule/config.service';

@Injectable()
export class CasStrategy extends PassportStrategy(Strategy, 'cas') {
  constructor(private configService: ConfigService) {
    super({
      version: 'CAS3.0',
      ssoBaseURL: 'https://login.ugent.be',
      serverBaseURL:
        configService.getCurrentConfiguration().auth.cas.serverBaseURL,
      validateURL: '/serviceValidate',
      useSaml: false,
    });
  }

  validate(body: any): SamlUser {
    return {
      firstName: body.attributes.givenname,
      lastName: body.attributes.surname,
      email: body.attributes.mail,
      id: body.attributes.ugentid,
      institution: Institution.UGENT,
    };
  }

  service(req: any): string {
    const configuration = this.configService.getCurrentConfiguration();
    const allowedCallbacks : string[] = configuration.auth.providers.map(provider => provider.callbackUrl);
    const callbackURL = req.query.callbackUrl || req.params.callbackURL;
    const baseURL = configuration.auth.cas.serverBaseURL;
    if (!callbackURL) {
      return baseURL + `/auth/login/cas/`;
    }
    if (allowedCallbacks.indexOf(callbackURL) === -1) {
      Logger.warn(`callback to URL ${callbackURL} is not allowed`);
      return baseURL + `/auth/login/cas/`;
    }
    const urlEncoded = encodeURIComponent(callbackURL);
    return baseURL + `/auth/login/cas/${urlEncoded}`;
  }
}
