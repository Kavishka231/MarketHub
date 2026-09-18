import axios from "axios";import{loadAuth,clearAuth}from "../features/auth/authStorage";
export const AUTH_UNAUTHORIZED_EVENT="markethub:unauthorized";
const client=axios.create({baseURL:import.meta.env.VITE_API_BASE_URL??"http://localhost:8080",headers:{"Content-Type":"application/json"}});
client.interceptors.request.use(config=>{const token=loadAuth()?.token;if(token)config.headers.Authorization="Bearer "+token;return config});
client.interceptors.response.use(response=>response,error=>{if(error.response?.status===401){clearAuth();window.dispatchEvent(new Event(AUTH_UNAUTHORIZED_EVENT))}return Promise.reject(error)});
export default client;