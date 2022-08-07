import { IsNotEmpty } from "class-validator";

class PasswordResetBody {
  @IsNotEmpty()
  token: string;
}

export class UnhashedPasswordResetBodyBase extends PasswordResetBody {
  @IsNotEmpty()
  password: string;
}

export class HashedPasswordResetBodyBase extends PasswordResetBody {
  @IsNotEmpty()
  hashedPassword: string;
}
