import { cookies } from "next/headers";
import { User } from "./users";

export type Article = {
  id: number;
  title: string;
  body: string;
  author: User;
  attachments: FileModel[];
}
export type FileModel = {
  id: number;
  name: string;
}

export async function fetchAllArticles(authorId?: number, timelineId?: number) {
  const urlSearchParams = new URLSearchParams({
  })
  if (authorId && authorId !== null) {
    urlSearchParams.set("author_id", authorId.toString())
  }
  if (timelineId && timelineId !== null) {
    urlSearchParams.set("timeline_id", timelineId.toString())
  }
  const url = `http://localhost:7070/api/articles?${urlSearchParams}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: cookies().toString() },
    next: { tags: ['articles'] }
  });
  if (!res.ok) {
    const error = new Error("Failed to fetch articles");
    throw error
  }

  if (res.ok) {
    const data = res.json();

    return data as Promise<Article[]>;
  }
}

export async function fetchArticle(id: string) {
  const url = `http://localhost:7070/api/articles/${id}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    headers: { Cookie: cookies().toString() },
  });
  if (!res.ok) {
    const error = new Error("Failed to get an article");
    throw error
  }

  if (res.ok) {
    const data = res.json();
    return data as Promise<Article>;
  }
}