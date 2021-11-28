import { IsEmail, IsNotEmpty } from "class-validator";

class LoginBodyBase {
  @IsEmail()
  email: string;
  @IsNotEmpty()
  password: string;
}

export class UnhashedLoginBodyBase extends LoginBodyBase {
  @IsNotEmpty()
  password: string;
}

export class HashedLoginBodyBase extends LoginBodyBase {
  @IsNotEmpty()
  hashedPassword: string;
}
