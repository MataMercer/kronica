import useSWR from "swr";
import { Character } from "../fetch/characters";
import { Page } from "../fetch/articles";

export async function fetchAllCharacters(authorId: number) {
  const urlSearchParams = new URLSearchParams({
    'author_id': authorId.toString()
  })
  const url = `http://localhost:7070/api/characters?${urlSearchParams}`;
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    next: { tags: ['characters'] }
  });
  if (!res.ok) {
    const error = new Error("Failed to fetch characters");
    throw error
  }

  if (res.ok) {
    const data = res.json();
    return data as Promise<Page<Character>>;
  }
}

export default function useCharacters(authorId?: number) {
  const { data, mutate, error } = useSWR(authorId ? ["useCharacters", authorId] : null, ([URL, authorId]) => fetchAllCharacters(authorId));

  const loading = !data && !error;
  return {
    loading,
    characters: data?.content,
    mutate
  };
}