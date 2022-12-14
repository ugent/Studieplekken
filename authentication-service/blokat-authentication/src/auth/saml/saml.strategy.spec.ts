import { Test, TestingModule } from '@nestjs/testing';
import { readMetadata, SamlStrategy } from './saml.strategy';
import * as fs from 'fs';
import * as path from 'path';
import { ConfigService } from '../../configModule/config.service';

describe('SamlService', () => {
  let service: SamlStrategy;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [SamlStrategy, ConfigService],
    }).compile();
    service = module.get<SamlStrategy>(SamlStrategy);
  });

  it('Should generate the metadata', () => {
    expect(service).toBeDefined();
  });

  it('Should generate the metadata', () => {
    const cert = fs
      .readFileSync(
        path.join(__dirname, '../../../config/auth/saml', './cert.pem'),
      )
      .toString();
    console.log(service.generateServiceProviderMetadata(cert, cert));
    expect(service.generateServiceProviderMetadata(cert, cert)).toBeDefined();
  });

  it('Should read metadata from a file', () => {
    expect(readMetadata('./meta-idp.xml')).toBeDefined();
  });
});
