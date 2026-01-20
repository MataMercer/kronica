import useSWR from "swr";
import { Page, Tag } from "../Types/Models";


export async function fetchAllTags(snippet: string) {
    const urlSearchParams = new URLSearchParams({
        'snippet': snippet
    })
    const url = `http://localhost:7070/api/tags/snippet${urlSearchParams}`;
    const res = await fetch(url, {
        method: "GET",
        credentials: "include",
        next: { tags: ['tags'] }
    });
    if (!res.ok) {
        console.log(res.status)
        const error = new Error("Failed to fetch tags");
        throw error
    }

    if (res.ok) {
        const data = res.json();
        return data as Promise<Page<Tag>>;
    }
}

export function useTags(snippet?: string) {
    const { data, mutate, error } = useSWR(snippet ? ["useTags", snippet] : null, ([URL, snippet]) => fetchAllTags(snippet));
    const loading = !data && !error;
    return {
        loading,
        tags: data,
        mutate
    };
}

const fetcher = (url: string) => fetch(url, {
    method: "GET",
    credentials: "include",
    next: { tags: ['tags'] }
}).then(res => res.json())
export function useTag(id: string) {
    const { data, mutate, error, isLoading } = useSWR(`http://localhost:7070/api/tags/snippet`, fetcher)
    return {
        tags: data?.content as Tag | undefined,
        mutate,
        isLoading,
        isError: error,
    }
}