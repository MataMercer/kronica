import useSWR from "swr";
import {Page} from "../fetch/articles";
import {Character} from "../Types/Models";

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
      throw new Error("Failed to fetch characters")
  }

  if (res.ok) {
    const data = res.json();
    return data as Promise<Page<Character>>;
  }
}

export function useCharacters(authorId?: number) {
  const { data, mutate, error } = useSWR(authorId ? ["useCharacters", authorId] : null, ([URL, authorId]) => fetchAllCharacters(authorId));

  const loading = !data && !error;
  return {
    loading,
    characters: data?.content,
    mutate
  };
}

export function useCharacter(id?: number) {
    const { data, mutate, error, isLoading } = useSWR(id ? `http://localhost:7070/api/characters/id/${id}` : null, (url) => fetch(url, {
        method: "GET",
        credentials: "include",
        next: { tags: ['character'] }
    }).then(res => res.json()));

    return {
        character: data as Character | undefined,
        mutate,
        isLoading,
        isError: error,
    };
}