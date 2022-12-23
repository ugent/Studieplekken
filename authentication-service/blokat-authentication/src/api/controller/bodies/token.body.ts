import { IsEnum } from "class-validator"

enum TokenPurpose {
    PASSWORD_RESET = "PASSWORD_RESET",
    REGISTRATION = "REGISTRATION"
}

export class TokenBody {
    @IsEnum(TokenPurpose)
    purpose: TokenPurpose
    email: string  
}