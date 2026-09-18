import type{AuthUser}from "../../types/auth";const KEY="markethub.auth";export interface StoredAuth{token:string;user:AuthUser}
export function loadAuth():StoredAuth|null{try{const value=localStorage.getItem(KEY);return value?JSON.parse(value) as StoredAuth:null}catch{return null}}
export function saveAuth(value:StoredAuth){localStorage.setItem(KEY,JSON.stringify(value))}
export function clearAuth(){localStorage.removeItem(KEY)}