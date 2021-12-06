import { Request } from "express";
import got from "got";
import { Strategy } from "passport-strategy";
import { URL } from "url";
import { XMLParser } from "fast-xml-parser";
import { Logger } from "@nestjs/common";

export interface CasStrategyOptions {
  ssoBaseURL: string;
  ssoLoginPath: string;
  ssoValidationPath: string;
  serverBaseURL: string;
}

export class CasStrategy extends Strategy {
  constructor(private options: CasStrategyOptions) {
    super();
  }

  authenticate(req: Request, options: any): void {
    const ticket = req.query.ticket as string;
    if (ticket) {
      this.validateTicket(req, ticket);
    } else {
      this.redirectToSso(req);
    }
  }

  /**
   * We need to send the user to the SSO endpoint to get a Service Ticket
   * @param req request parameter
   */
  private redirectToSso(req: Request) {
    const serviceUrl = new URL(
      this.options.ssoLoginPath,
      this.options.ssoBaseURL,
    );
    serviceUrl.searchParams.set("service", this.service(req, this.options));

    this.redirect(serviceUrl.href);
  }

  /**
   * The user has returned from the SSO endpoint with a ticket.
   * We need to validate the ticket, and then log the user in.
   * @param req request object
   * @param ticket SSO ticket that we need to validate.
   */
  private async validateTicket(req: Request, ticket: string) {
    const validationUrl = new URL(
      this.options.ssoValidationPath,
      this.options.ssoBaseURL,
    );

    validationUrl.searchParams.set("service", this.service(req, this.options));
    validationUrl.searchParams.set("ticket", ticket);

    try {
      const result = await got.get(validationUrl, { responseType: "text" });

      const parsedJsObject = this.xmlToJs(result.body);

      // This is an unexpected error. There should always be a response from the CAS server.
      if (!parsedJsObject["cas:serviceResponse"])
        return this.error(
          new Error(`Unexpected response from CAS server: ${result.body}`),
        );

      const responseContent = parsedJsObject["cas:serviceResponse"] as Record<
        string,
        unknown
      >;

      // This is a somewhat expected error: there may be cases where the ticket fails. Give a good error before failing.
      if (responseContent.hasOwnProperty("cas:authenticationFailure")) {
        Logger.warn(
          `Failed this authentication: ${responseContent["cas:authenticationFailure"]}`,
        );
        return this.fail("Ticket validation failed", 401);
      } else if (responseContent.hasOwnProperty("cas:authenticationSuccess")) {
        // Expected success flow
        const body = responseContent["cas:authenticationSuccess"];
        const user = this.validate(body);
        return this.success(user);
      } else {
        // Unexpected error flow
        throw new Error(
          `This is an unexpected error, body is ${responseContent}`,
        );
      }
    } catch (e: any) {
      Logger.error("The request to the service endpoint failed.");
      Logger.error(
        `The service URL was ${this.service(
          req,
          this.options,
        )} and the ticket ${ticket}`,
      );
      Logger.error(`The full request URL was ${validationUrl}`);
      this.error(e);
    }
  }

  private xmlToJs(body: string): Record<string, unknown> {
    const parser = new XMLParser({
      numberParseOptions: { leadingZeros: false, hex: false },
    });
    return parser.parse(body);
  }

  protected service(req: Request, options: CasStrategyOptions) {
    return options.serverBaseURL;
  }

  protected validate(body: unknown) {
    return body;
  }
}
