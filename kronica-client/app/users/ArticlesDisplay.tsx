import ArticleThumb from "@/app/components/articles/ArticleThumb";
import { fetchAllArticles } from "@/app/fetch/articles";
import KronologizeButton from "./EditTimelineButton";
import { fetchTimeline } from "../fetch/timelines";

type ArticleDisplayProps = {
    timelineId: number;
};
export default async function ArticleDisplay({
    timelineId,
}: ArticleDisplayProps) {
    const articles = (await fetchAllArticles(undefined, timelineId))?.content;
    const timeline = timelineId
        ? await fetchTimeline(timelineId.toString())
        : undefined;
    return (
        <div className=" grow p-4">
            {timeline && (
                <>
                    <h1 className="text-center text-4xl border-black border-b-[1px]">
                        {timeline.name}
                    </h1>
                    <p>{timeline.description}</p>

                    <div className="text-3xl">Starring</div>
                    <div>No characters</div>
                </>
            )}
            <div className="text-3xl">Articles</div>
            <KronologizeButton timelineId={timelineId} />
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
