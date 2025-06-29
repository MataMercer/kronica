import { cookies } from "next/headers";
import { FileModel, Page } from "./articles";
import { User } from "../Types/Models";


export type Character = {
  id: number;
  age: number;
  status: string;
  gender: string;
  firstSeen: string;
  birthday: string;
  name: string;
  body: string;
  author: User;
  attachments: FileModel[];
  profilePictures: FileModel[];
  traits: {};
}

export async function fetchCharacter(id: string) {
  const url = `http://localhost:7070/api/characters/${id}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: (await cookies()).toString() },
  });
  if (!res.ok) {
    const error = new Error("Failed to get an character");
    throw error
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
  if (authorId && authorId !== null) {
    urlSearchParams.set("author_id", authorId.toString());
  }
  if (timelineId && timelineId !== null) {
    urlSearchParams.set("timeline_id", timelineId.toString());
  }
  const url = `http://localhost:7070/api/characters?${urlSearchParams}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: (await cookies()).toString() },
  });
  if (!res.ok) {
    const error = new Error("Failed to fetch characters");
    throw error;
  }

  if (res.ok) {
    const data = res.json();
    return data as Promise<Page<Character>>;
  }
}