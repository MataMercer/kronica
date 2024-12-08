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
        <div className="grow">
            {timeline && (
                <>
                    <h1 className="m-2 p-2 text-center font-extrabold text-4xl border-black border-[1px]">
                        {timeline.name}
                    </h1>
                    <p>{timeline.description}</p>

                    <div className="text-3xl">Starring</div>
                    <div>No characters</div>
                </>
            )}
            <div className="flex space-x-2">
                <div className="text-3xl">Articles</div>
                <KronologizeButton timelineId={timelineId} />
            </div>
            <div className="grid sm:grid-cols-5 gap-1">
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
