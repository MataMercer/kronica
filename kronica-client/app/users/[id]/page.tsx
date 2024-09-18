import { fetchArticle } from "@/app/fetch/articles";
import Image from "next/image";

export default async function UserProfilePage({
    params,
}: {
    params: { id: string };
}) {
    const id = params.id;
    const article = id && (await fetchArticle(id));

    return <div>{article && <div>user stuff</div>}</div>;
}
