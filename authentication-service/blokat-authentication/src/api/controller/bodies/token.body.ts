import { IsEnum, IsNotEmpty } from "class-validator"

enum TokenPurpose {
    PASSWORD_RESET = "PASSWORDRESET",
    REGISTRATION = "REGISTRATION"
}

export class TokenBody {
    @IsEnum(TokenPurpose)
    purpose: TokenPurpose
    @IsNotEmpty()
    email: string  
}