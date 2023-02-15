import { Test, TestingModule } from '@nestjs/testing';
import { ConfigModule } from 'src/configModule/config.module';
import { DbModule } from 'src/db/db.module';
import { DbService } from 'src/db/db.service';
import { TokenController } from './token.controller';
import * as request from 'supertest';
import { INestApplication } from '@nestjs/common';
import { sign } from 'jsonwebtoken';
import { getConfig } from 'src/configModule/config.service';

const header = () => {
  const jwt = sign({}, getConfig().backendServiceJwtKey)
  const header = `Bearer ${jwt}`
  return header
}

describe('ApiController', () => {
  let app: INestApplication;


  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [TokenController],
      imports: [DbModule, ConfigModule]
    }).compile();

    const dbService = module.get<DbService>(DbService);
    dbService.wipe();


    app = module.createNestApplication();
    await app.init();
  });
  


  it('should error when no jwt is given', () => {
    return request(app.getHttpServer()).get("/auth/tokens").expect(403)
  })

  it('should give empty', () => {
    return request(app.getHttpServer()).get("/auth/tokens").set('Authorization', header()).expect({"tokens": []})
  })

  it('should reject invalid purpose', () => {
    const token = {email: "maxiem@maxiemgeldhof.com", purpose: "wrong purpose"}
    const tokenRequest = request(app.getHttpServer())
                .post("/auth/tokens")
                .set('Authorization', header())
                .set('Accept', 'application/json')
                .send(token)
                .expect(400)
    return tokenRequest
  })

  it('should accept token and create', async () => {
    const token = {email: "maxiem@maxiemgeldhof.com", purpose: "PASSWORD_RESET"}
    const postTokenRequest = await request(app.getHttpServer())
                .post("/auth/tokens")
                .set('Authorization', header())
                .set('Accept', 'application/json')
                .send(token)
                .expect(201)

    const getTokenRequest = await request(app.getHttpServer())
                .get("/auth/tokens")
                .set('Authorization', header())
                .set('Accept', 'application/json')
                .expect(200)
    expect(getTokenRequest.body.tokens.length).toBe(1)

    return getTokenRequest
  })
});
