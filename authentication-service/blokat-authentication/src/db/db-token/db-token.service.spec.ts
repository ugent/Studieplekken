import { Test, TestingModule } from '@nestjs/testing';
import { ConfigService } from 'src/configModule/config.service';
import { DbService } from '../db.service';
import { DbTokenService } from './db-token.service';

describe('DbTokenService', () => {
  let service: DbTokenService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [DbTokenService, DbService, ConfigService],
    }).compile();

    service = module.get<DbTokenService>(DbTokenService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
