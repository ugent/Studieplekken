import { Test, TestingModule } from "@nestjs/testing";
import { ConfigService } from "../../configModule/config.service";
import { DbService } from "../db.service";
import { DbUserService } from "./db-user.service";

describe("DbUserService", () => {
  let service: DbUserService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [DbUserService, DbService, ConfigService],
    }).compile();

    const dbService = module.get<DbService>(DbService);
    dbService.wipe();

    service = module.get<DbUserService>(DbUserService);
  });

  it("should be defined", () => {
    expect(service).toBeDefined();
  });

  it("Should create and fetch my user", async () => {
    const createUser = await service.create({email: "maxiem.geldhof@ugent.be", first_name: "Maxiem", last_name: "Geldhof", user_id: "0", hashed_password: "", salt: ""})

    const me = await service.userByEmail("maxiem.geldhof@ugent.be");
    expect(me.first_name).toEqual(createUser.first_name);
  });


  it("Should not find nonexistent user", async () => {
    const me = await service.userByEmail("maxiem.geldhof@ugent.be");
    expect(me).toEqual(null);
  });
});
