import { Body, Controller, Post, ValidationPipe } from "@nestjs/common";
import { RegisterFlowService } from "./register-flow/register-flow-service";
import { UnhashedRegisterBodyBase } from "./register-flow/RegisterBodyInterface";

@Controller("/auth/local")
export class LocalFlowController {
  constructor(private registerFlow: RegisterFlowService) {}

  @Post("register")
  registerNewAccount(@Body(ValidationPipe) body: UnhashedRegisterBodyBase) {
    return this.registerFlow.handleRegistration(body);
  }
}
