import { Test, TestingModule } from '@nestjs/testing';
import { DbTokenService } from './db-token.service';

describe('DbTokenService', () => {
  let service: DbTokenService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [DbTokenService],
    }).compile();

    service = module.get<DbTokenService>(DbTokenService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
