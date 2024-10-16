import ArticleThumb from "@/app/components/articles/ArticleThumb";
import { fetchAllArticles } from "@/app/fetch/articles";

type ArticleDisplayProps = {
    timelineId: number;
};
export default async function ArticleDisplay({
    timelineId,
}: ArticleDisplayProps) {
    const articles = await fetchAllArticles(undefined, timelineId);
    return (
        <div className=" grow p-4">
            <div className="text-3xl">Starring</div>
            <div>No one is listed as starring in this timeline.</div>
            <div className="text-3xl">Articles</div>
            <button className="button">Kronologize</button>
            <div className="grid sm:grid-cols-5 gap-4">
                {articles && articles.length > 0 ? (
                    articles.map((article) => (
                        <ArticleThumb key={article.id} article={article} />
                    ))
                ) : (
                    <div>There are no articles.</div>
                )}
            </div>
        </div>
    );
}
