import { Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { getConfig } from '../configModule/config.service';
import { ConfigModule } from '../configModule/config.module';
import { DbModule } from '../db/db.module';
import { AuthService } from './auth.service';
import { CasStrategy } from './cas/cas.strategy';
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
  providers: [AuthService, SamlStrategy, CasStrategy],
  exports: [AuthService],
})
export class AuthModule {}
