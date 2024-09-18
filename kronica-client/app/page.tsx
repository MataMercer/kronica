import Image from "next/image";
import { fetchAllArticles } from "./fetch/articles";
import CreateArticleButton from "./CreateArticleButton";
import ArticleThumb from "./components/articles/ArticleThumb";
import AuthProtectionSSR from "./AuthProtectionSSR";
import SortableInput from "./components/inputs/SortableInput";
import CreateTimelineButton from "./CreateTimelineButton";
import { fetchAllTimelines } from "./fetch/timelines";

export default async function Home() {
    const articles = await fetchAllArticles();
    const timelines = await fetchAllTimelines();

    console.log(articles);
    return (
        <>
            <h1 className="text-3xl self-center ">HOME</h1>
            <AuthProtectionSSR>
                <div className="self-center flex">
                    <div className="m-2 ">
                        <CreateArticleButton />
                    </div>
                    <div className="m-2 ">
                        <CreateTimelineButton />
                    </div>
                </div>
            </AuthProtectionSSR>

            <h2 className="text-2xl border-black border-b-[1px] mb-10">
                NEW ARTICLES
            </h2>
            <div className="grid sm:grid-cols-5 gap-4">
                {articles ? (
                    articles.map((article) => (
                        <ArticleThumb key={article.id} article={article} />
                    ))
                ) : (
                    <div>There are no articles.</div>
                )}
            </div>
            <div className="grid sm:grid-cols-5 gap-4">
                {timelines ? (
                    timelines.map((timeline) => (
                        <div key={timeline.id}>{timeline.name}</div>
                    ))
                ) : (
                    <div>There are no articles.</div>
                )}
            </div>
        </>
    );
}
