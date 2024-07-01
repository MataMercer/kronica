import useSWR from "swr";

export async function fetchCurrentUser() {
  const url = "http://localhost:7070/api/auth/currentuser";
  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    next: { tags: ['currentuser'] }
  });

  if (res.ok) {
    const data = res.json();
    return data;
  }
}

export default function useCurrentUser() {
  const { data, mutate, error } = useSWR("currentuser", fetchCurrentUser);

  const loading = !data && !error;
  const loggedOut = error;
  return {
    loading,
    loggedOut,
    user: data,
    mutate
  };
}
