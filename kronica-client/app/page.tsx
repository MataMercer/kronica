import Image from "next/image";
import { fetchAllArticles } from "./fetch/articles";
import CreateArticleButton from "./CreateArticleButton";
import ArticleThumb from "./components/articles/ArticleThumb";
import AuthProtectionSSR from "./AuthProtectionSSR";

export default async function Home() {
    const articles = await fetchAllArticles();

    return (
        <>
            <h1 className="text-3xl self-center ">HOME</h1>
            <AuthProtectionSSR>
                <div className="m-10 self-center">
                    <CreateArticleButton />
                </div>
            </AuthProtectionSSR>

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
        </>
    );
}
