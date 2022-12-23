import { Test, TestingModule } from '@nestjs/testing';
import { ConfigModule } from 'src/configModule/config.module';
import { BackendServiceGuard } from './backend-service.guard';
import { sign } from 'jsonwebtoken';
import { getConfig } from 'src/configModule/config.service';

describe('ServiceJwtService', () => {
  let service: BackendServiceGuard;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [BackendServiceGuard],
      imports: [ConfigModule]
    }).compile();

    service = module.get<BackendServiceGuard>(BackendServiceGuard);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  it("should invalidate non-bearer header", () => {
    const header = "header"
    expect(service.validateBackendHeader(header)).toBeFalsy()
  })

  it("should invalidate non-valid jwt header", () => {
    const header = "Bearer invalid"
    expect(service.validateBackendHeader(header)).toBeFalsy()
  })

  it("should invalidate jwt signed with wrong key", () => {
    const jwt = sign({}, getConfig().backendServiceJwtKey + 'n')
    const header = `Bearer ${jwt}`
    expect(service.validateBackendHeader(header)).toBeFalsy()
  })


  it("should accept correct jwt", () => {
    const jwt = sign({}, getConfig().backendServiceJwtKey)
    const header = `Bearer ${jwt}`
    expect(service.validateBackendHeader(header)).toBeTruthy()
  })
});
