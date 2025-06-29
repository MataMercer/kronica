import { cookies } from "next/headers";
import { User } from "../Types/Models";
import { Character } from "./characters";
import { Timeline } from "./timelines";

export type Page<T> = {
  content: T[]
  pages: number
}

export type Article = {
  id: number;
  title: string;
  body: string;
  author: User;
  attachments: FileModel[];
  characters: Character[];
  timeline: Timeline;
  youLiked: boolean;
  likeCount: number;
}
export type FileModel = {
  id: number;
  name: string;
  caption: string;
  storageId: string;
}

export async function fetchAllArticles(authorId?: number, timelineId?: number, page?: number) {
  const urlSearchParams = new URLSearchParams({
  })
  if (authorId && authorId !== null) {
    urlSearchParams.set("author_id", authorId.toString())
  }
  if (timelineId && timelineId !== null) {
    urlSearchParams.set("timeline_id", timelineId.toString())
  }
  if (page && page !== null) {
    urlSearchParams.set("page", (page - 1).toString())
    // urlSearchParams.set("size", "20")
  }
  const url = `http://localhost:7070/api/articles?${urlSearchParams}`;
  console.log(url)
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: (await cookies()).toString() },
    next: { tags: ['articles'] }
  });
  if (!res.ok) {
    const error = new Error("Failed to fetch articles");
    throw error
  }

  if (res.ok) {
    const data = res.json();

    return data as Promise<Page<Article>>;
  }
}

export async function fetchArticle(id: string) {
  const url = `http://localhost:7070/api/articles/id/${id}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: (await cookies()).toString() },
  });
  if (!res.ok) {
    const error = new Error("Failed to get an article. HTTP Error:" + res.status);
    throw error
  }

  if (res.ok) {
    const data = res.json();
    return data as Promise<Article>;
  }
}