import { Injectable, Logger } from "@nestjs/common";
import { PassportStrategy } from "@nestjs/passport";
import { CasStrategy } from "./cas.strategy";
import { Institution, SamlUser } from "src/configModule/config";
import { ConfigService } from "src/configModule/config.service";

@Injectable()
export class CasStrategyService extends PassportStrategy(CasStrategy, "cas") {
  constructor(private configService: ConfigService) {
    super({
      ssoBaseURL: "https://login.ugent.be",
      serverBaseURL:
        configService.getCurrentConfiguration().auth.cas.serverBaseURL,
      ssoValidationPath: "/serviceValidate",
      ssoLoginPath: "/",
    });
  }

  validate(body: any): SamlUser {
    const attributes = body["cas:attributes"];
    return {
      firstName: attributes["cas:givenname"],
      lastName: attributes["cas:surname"],
      email: attributes["cas:mail"],
      id: attributes["cas:ugentID"],
      institution: Institution.UGENT,
    };
  }

  service(req: any): string {
    const configuration = this.configService.getCurrentConfiguration();
    const allowedCallbacks: string[] =
      configuration.auth.allowedClientCallbacks;
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
