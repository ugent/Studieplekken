import { Test, TestingModule } from '@nestjs/testing';
import { ConfigService } from '../configModule/config.service';
import { DbService } from './db.service';

describe('DbService', () => {
  let service: DbService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [DbService, ConfigService],
    }).compile();

    service = module.get<DbService>(DbService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
