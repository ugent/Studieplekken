import { Test, TestingModule } from '@nestjs/testing';
import { DbService } from '../db.service';
import { DbUserService } from './db-user.service';

describe('DbUserService', () => {
  let service: DbUserService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [DbUserService, DbService],
    }).compile();

    service = module.get<DbUserService>(DbUserService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  it('Should fetch my user', async () => {
    const me = await service.userById('000170335535');
    expect(me.first_name).toEqual('Maxiem');
    expect(me.admin).toEqual(true);
  });
});
