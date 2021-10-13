import { Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { ConfigModule } from '../configModule/config.module';
import { DbModule } from '../db/db.module';
import { AuthService } from './auth.service';
import { CasStrategy } from './cas/cas.strategy';
import { jwtConstants } from './constants';
import { SamlStrategy } from './saml/saml.strategy';

@Module({
  imports: [
    PassportModule,
    JwtModule.register({
      secret: jwtConstants.secret,
      signOptions: { expiresIn: '60s' },
    }),
    DbModule,
    ConfigModule,
  ],
  providers: [AuthService, SamlStrategy, CasStrategy],
  exports: [AuthService],
})
export class AuthModule {}
