import { IsEmail, IsNotEmpty } from "class-validator";

class RegisterBodyBase {
  @IsEmail()
  email: string;
  @IsNotEmpty()
  first_name: string;
  @IsNotEmpty()
  last_name: string;
  @IsNotEmpty()
  token: string;
}

export class UnhashedRegisterBodyBase extends RegisterBodyBase {
  @IsNotEmpty()
  password: string;
}

export class HashedRegisterBodyBase extends RegisterBodyBase {
  @IsNotEmpty()
  hashedPassword: string;
}
