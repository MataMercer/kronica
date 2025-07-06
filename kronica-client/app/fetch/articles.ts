import { cookies } from "next/headers";
import {Article} from "@/app/Types/Models";

export type Page<T> = {
  content: T[]
  pages: number
}

export async function fetchAllArticles(authorId?: number, timelineId?: number, page?: number) {
  const urlSearchParams = new URLSearchParams({
  })
  if (authorId) {
    urlSearchParams.set("author_id", authorId.toString())
  }
  if (timelineId) {
    urlSearchParams.set("timeline_id", timelineId.toString())
  }
  if (page) {
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