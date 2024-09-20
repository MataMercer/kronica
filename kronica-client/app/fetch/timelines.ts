
import { cookies } from "next/headers";
import { User } from "./users";

export type Timeline = {
  id: number;
  name: string;
  description: string;
  author: User;
}

export async function fetchAllTimelines(authorId: number) {
  const urlSearchParams = new URLSearchParams({
    'author_id': authorId.toString()
  })
  const url = `http://localhost:7070/api/timelines?${urlSearchParams}`;
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