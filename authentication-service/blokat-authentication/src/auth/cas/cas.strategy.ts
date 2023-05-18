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
  /**
   * Authentication strategy to log in for CAS.
   * CAS is a protocol used for authentication within UGent.
   * @param options Strategy options.
   */

  constructor(private options: CasStrategyOptions) {
    super();
  }

  authenticate(req: Request, options: any): void {
    /** Entry point of the CAS endpoint.
     *  CAS flow is as follows:
     * - This Service will redirect you to the external CAS login page
     * - You log in to the page, and it redirects you with a ticket
     * - This app will use the ticket to fetch authentication information from the server.
     *
     * In this flow, the app is called twice: once originally by the user, then again with the ticket.
     * Split up the logic for these two calls in `validateTicket` and `redirectToSso`.
     */
    const ticket = req.query.ticket as string;
    if (ticket) {
      // Fetch the user's information from the CAS server using the ticket received from the service.
      this.validateTicket(req, ticket);
    } else {
      // Redirect the user to the CAS login page to get a ticket.
      // It will redirect back here to this function, but with a ticket.
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
      // Retrieve user information
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
      }

      if (!responseContent.hasOwnProperty("cas:authenticationSuccess")) {
        throw new Error(
          `This is an unexpected error, body is ${responseContent}`,
        );
      }

      // The request was a success, and contains expected data.
      // We'll parse out the user information and return it.
      const body = responseContent["cas:authenticationSuccess"];
      const user = this.validate(body);
      return this.success(user);
    } catch (e: any) {
      // Very unexpected error, and probably a coding error
      // Should be looked into.

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
