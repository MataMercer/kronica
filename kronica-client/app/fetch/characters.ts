import {cookies} from "next/headers";
import {Page} from "./articles";
import {Character} from "../Types/Models";


export async function fetchCharacter(id: string) {
  const url = `http://localhost:7070/api/characters/id/${id}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: (await cookies()).toString() },
  });
  if (!res.ok) {
    throw new Error("Failed to get an character")
  }

  if (res.ok) {
    const data = res.json();
    return data as Promise<Character>;
  }
}

export async function fetchCharacters(
  authorId?: number,
  timelineId?: number
) {
  const urlSearchParams = new URLSearchParams({});
  if (authorId) {
    urlSearchParams.set("author_id", authorId.toString());
  }
  if (timelineId) {
    urlSearchParams.set("timeline_id", timelineId.toString());
  }
  const url = `http://localhost:7070/api/characters?${urlSearchParams}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: (await cookies()).toString() },
  });
  if (!res.ok) {
    throw new Error("Failed to fetch characters");
  }

  if (res.ok) {
    const data = res.json();
    return data as Promise<Page<Character>>;
  }
}