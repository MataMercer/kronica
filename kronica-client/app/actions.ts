'use server'

import { revalidateTag } from "next/cache";

export async function refreshCurrentUser() {
  revalidateTag("currentuser");
}

export async function refreshArticles() {
  revalidateTag('articles')
}