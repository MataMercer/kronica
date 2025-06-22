import { cookies } from "next/headers";
import { FileModel } from "./articles";
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