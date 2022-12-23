import { Test, TestingModule } from '@nestjs/testing';
import { AppController } from './app.controller';
import { AuthModule } from './auth/auth.module';
import { ConfigModule } from './configModule/config.module';
import { DbModule } from './db/db.module';

describe('AppController', () => {
  let appController: AppController;

  beforeEach(async () => {
    const app: TestingModule = await Test.createTestingModule({
      controllers: [AppController],
      imports: [AuthModule, DbModule, ConfigModule],
    }).compile();

    appController = app.get<AppController>(AppController);
  });

  describe('root', () => {
    it('Should exist', () => {
      expect(appController).toBeDefined();
    });
  });
});
