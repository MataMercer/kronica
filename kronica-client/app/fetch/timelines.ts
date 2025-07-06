
import { cookies } from "next/headers";
import {Timeline} from "../Types/Models";

export async function fetchAllTimelines(authorId: number) {
  const urlSearchParams = new URLSearchParams({
    'author_id': authorId.toString()
  })
  const url = `http://localhost:7070/api/timelines?${urlSearchParams}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: (await cookies()).toString() },
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

export async function fetchTimeline(id: string) {
  const url = `http://localhost:7070/api/timelines/id/${id}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: (await cookies()).toString() },
  });
  if (!res.ok) {
    const error = new Error("Failed to get a timeline");

    console.log(res.status)
    console.log(error.message)
  }

  if (res.ok) {
    const data = res.json();
    return data as Promise<Timeline>;
  }
}