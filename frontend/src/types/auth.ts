export type UserRole="CUSTOMER"|"VENDOR"|"ADMIN";export type UserStatus="ACTIVE"|"DISABLED";
export interface AuthUser{id:number;firstName:string;lastName:string;email:string;phone?:string|null;role:UserRole;status:UserStatus}
export interface LoginRequest{email:string;password:string} export interface LoginResponse{token:string;tokenType:string;expiresIn:number;user:AuthUser}
export interface RegisterRequest{firstName:string;lastName:string;email:string;phone?:string;password:string} export interface RegisterResponse extends AuthUser{createdAt:string}