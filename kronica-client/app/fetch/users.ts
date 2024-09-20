import { cookies } from "next/headers";

export type User = {
  id: number;
  name: string;
  role: string;
}

export async function fetchUser(id: string) {
  const url = `http://localhost:7070/api/users/${id}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: cookies().toString() },
  });
  if (!res.ok) {
    const error = new Error("Failed to get a user");
    throw error
  }

  if (res.ok) {
    const data = res.json();
    return data as Promise<User>;
  }
}