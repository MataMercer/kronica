import { cookies } from "next/headers";
import { PublicUser } from "./PublicUsers";

export type Article = {
  id: number;
  title: string;
  body: string;
  author: PublicUser;
  attachments: string[];
}

export async function fetchAllArticles() {
  const url = "http://localhost:7070/api/articles";
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