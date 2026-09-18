import client from "./client";import type{AuthUser,LoginRequest,LoginResponse,RegisterRequest,RegisterResponse}from "../types/auth";
export async function loginRequest(input:LoginRequest){return(await client.post<LoginResponse>("/api/auth/login",input)).data}
export async function registerRequest(input:RegisterRequest){return(await client.post<RegisterResponse>("/api/auth/register",input)).data}
export async function getCurrentUser(){return(await client.get<AuthUser>("/api/auth/me")).data}