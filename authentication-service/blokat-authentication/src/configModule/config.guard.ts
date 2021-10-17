import { CanActivate, Injectable } from '@nestjs/common';
import { ConfigService } from './config.service';

@Injectable()
export class ConfigGuard implements CanActivate {
  constructor(private configService: ConfigService) {}

  canActivate() {
    return this.configService.getCurrentConfiguration().auth.testEndpoint;
  }
}
