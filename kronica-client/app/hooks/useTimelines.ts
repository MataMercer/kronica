import useSWR from "swr";
import { Timeline } from "../fetch/timelines";


export async function fetchAllTimelines(authorId: number) {
  const urlSearchParams = new URLSearchParams({
    'author_id': authorId.toString()
  })
  const url = `http://localhost:7070/api/timelines?${urlSearchParams}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
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

export default function useTimelines(authorId?: number) {
  const { data, mutate, error } = useSWR(authorId ? ["useTimelines", authorId] : null, ([URL, authorId]) => fetchAllTimelines(authorId));

  const loading = !data && !error;
  return {
    loading,
    timelines: data,
    mutate
  };
}