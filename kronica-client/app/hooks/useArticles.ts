import useSWR from "swr";
import { Timeline } from "../fetch/timelines";
import { Article, Page } from "../fetch/articles";


export async function fetchAllArticles(authorId?: string, timelineId?: string) {
  const urlSearchParams = new URLSearchParams({
    'author_id': authorId || "",
    'timeline_id': timelineId || ""
  })
  const url = `http://localhost:7070/api/articles?${urlSearchParams}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
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

export async function fetchFollowedArticles() {
  const urlSearchParams = new URLSearchParams({

  })
  const url = `http://localhost:7070/api/articles?${urlSearchParams}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    next: { tags: ['articles'] }
  });
  if (!res.ok) {
    console.log(res.status)
    const error = new Error("Failed to fetch articles");
    throw error
  }

  if (res.ok) {
    const data = res.json();
    return data as Promise<Page<Article>>;
  }

}

export function useArticles(authorId?: string, timelineId?: string) {
  const { data, mutate, error } = useSWR(authorId ? ["useArticles", authorId] : null, ([URL, authorId]) => fetchAllArticles(authorId, timelineId));
  const loading = !data && !error;

  return {
    loading,
    articles: data?.content,
    mutate
  };
}

export function useFollowedArticles() {
  const { data, mutate, error } = useSWR(["useFollowedArticles"], ([]) => fetchFollowedArticles());
  const loading = !data && !error;

  return {
    loading,
    articles: data?.content,
    mutate
  };
}

const fetcher = (url: string) => fetch(url, {
  method: "GET",
  credentials: "include",
  next: { tags: ['article'] }
}).then(res => res.json())
export function useArticle(id: string) {
  const { data, mutate, error, isLoading } = useSWR(`http://localhost:7070/api/articles/id/${id}`, fetcher)

  return {
    article: data as Article,
    mutate,
    isLoading,
    isError: error
  }

}