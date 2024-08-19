import { fetchArticle } from "@/app/fetch/articles";

export default async function ArticlePage({
    params,
}: {
    params: { id: string };
}) {
    const id = params.id;
    const article = id && (await fetchArticle(id));

    return (
        <div>
            <h1 className="text-3xl font-bold">{article && article.title}</h1>
            <p>{article && article.body}</p>
        </div>
    );
}
