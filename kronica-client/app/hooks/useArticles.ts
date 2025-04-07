import useSWR from "swr";
import { Timeline } from "../fetch/timelines";
import { Article, Page } from "../fetch/articles";


export async function fetchAllArticles(authorId?: string, timelineId?: string) {
  const urlSearchParams = new URLSearchParams({
    'author_id': authorId || "",
    // 'timeline_id': timelineId || ""
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

export default function useArticles(authorId?: string, timelineId?: string) {
  const { data, mutate, error } = useSWR(authorId ? ["useArticles", authorId] : null, ([URL, authorId]) => fetchAllArticles(authorId, timelineId));
  const loading = !data && !error;

  return {
    loading,
    articles: data?.content,
    mutate
  };
}