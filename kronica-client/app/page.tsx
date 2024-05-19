import Image from "next/image";
import { fetchAllArticles } from "./fetch/articles";
import CreateArticleButton from "./CreateArticleButton";
import ArticleThumb from "./components/articles/ArticleThumb";

export default async function Home() {
    const articles = await fetchAllArticles();

    return (
        <main className="flex min-h-screen flex-col p-10">
            <h1 className="text-3xl self-center ">HOME</h1>
            <div className="m-10 self-center">
                <CreateArticleButton />
            </div>
            <h2 className="text-2xl border-black border-b-[1px] mb-10">
                NEW ARTICLES
            </h2>
            <div className="grid sm:grid-cols-4 gap-4">
                {articles ? (
                    articles.map((article) => (
                        <ArticleThumb key={article.id} article={article} />
                    ))
                ) : (
                    <div>There are no articles.</div>
                )}
            </div>
        </main>
    );
}
