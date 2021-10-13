import { Injectable } from '@nestjs/common';
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

  service(req: any) {
    const urlEncoded = encodeURIComponent(
      req.query.callbackURL || req.params.callbackURL,
    );

    const final =
      urlEncoded === 'undefined'
        ? `${
            this.configService.getCurrentConfiguration().auth.cas.serverBaseURL
          }/login/cas/`
        : `${
            this.configService.getCurrentConfiguration().auth.cas.serverBaseURL
          }/login/cas/${urlEncoded}/`;

    return final;
  }
}
