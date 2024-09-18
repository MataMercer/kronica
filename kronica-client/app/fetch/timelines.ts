
import { cookies } from "next/headers";
import { User } from "./PublicUsers";

export type Timeline = {
  id: number;
  name: string;
  description: string;
  author: User;
}

export async function fetchAllTimelines() {
  const url = "http://localhost:7070/api/timelines";
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: cookies().toString() },
    next: { tags: ['timelines'] }
  });
  if (!res.ok) {
    console.log(res.status)
    const error = new Error("Failed to fetch timelines");
    throw error
  }

  if (res.ok) {
    const data = res.json();

    return data as Promise<Timeline[]>;
  }
}