import { Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { getConfig } from '../configModule/config.service';
import { ConfigModule } from '../configModule/config.module';
import { DbModule } from '../db/db.module';
import { AuthService } from './auth.service';
import { CasStrategyService } from './cas/cas.strategy.service';
import { SamlStrategy } from './saml/saml.strategy';

@Module({
  imports: [
    PassportModule,
    JwtModule.register({
      secret: getConfig().jwtKey,
      signOptions: { expiresIn: '5 days' },
    }),
    DbModule,
    ConfigModule,
  ],
  providers: [AuthService, SamlStrategy, CasStrategyService],
  exports: [AuthService],
})
export class AuthModule {
  /**
   * The Auth module is responsible for handling the authentication strategies.
   * It provides a CAS strategy, and a MultiSaml strategy.
   */
}
