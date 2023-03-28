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
    const id = attributes["cas:ugentStudentID"]
      ? this.calculateEAN13UgentStudentID(attributes["cas:ugentStudentID"])
      : attributes["cas:ugentID"];

    return {
      firstName: attributes["cas:givenname"],
      lastName: attributes["cas:surname"],
      email: attributes["cas:mail"],
      id: id,
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

  calculateEAN13UgentStudentID(barcode: string) {
    // Check that the barcode is 8 digits long
    if (barcode.length !== 8) {
      throw new Error("Barcode must be 8 digits long");
    }

    // Get the modulo 97 of the digit.
    let modulo = parseInt(barcode) % 97;

    // If modulo is 0, the modulo digit is 97
    if (modulo === 0) {
      modulo = 97;
    }

    // Append the modulo digit to the barcode as the 9th and 10th digit
    barcode += modulo.toString().padStart(2, "0");

    // Add 2 leading zeros
    barcode = "00" + barcode;

    // Return the barcode
    return barcode;
  }
}
