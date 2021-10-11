import { Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { ConfigModule } from '../configModule/config.module';
import { DbModule } from '../db/db.module';
import { AuthService } from './auth.service';
import { jwtConstants } from './constants';
import { LocalStrategy } from './local/local.strategy';
import { SamlStrategy } from './saml/saml.strategy';

@Module({
  imports: [
    PassportModule,
    JwtModule.register({
      secret: jwtConstants.secret,
      signOptions: { expiresIn: '60s' },
    }),
    DbModule,
    ConfigModule
    ],
  providers: [AuthService, LocalStrategy, SamlStrategy],
  exports: [AuthService],
})
export class AuthModule {}
