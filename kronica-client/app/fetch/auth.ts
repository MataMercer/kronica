import { cookies } from "next/headers";

export type PrivateUser = {
  id: number;
  name: string;
  email: string;
  role: string;
}

export async function fetchCurrentUser() {
  const url = "http://localhost:7070/api/auth/currentuser";
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: cookies().toString() },
    next: { tags: ['currentuser'] }
  });
  if (!res.ok) {
    const error = new Error("HTTP" + res.status);
    throw error
  }

  if (res.ok) {
    const data = res.json();
    return data;
  }
}