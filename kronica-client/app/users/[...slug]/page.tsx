import TimelineSideBar from "../TimelineSideBar";
import ArticleDisplay from "../ArticlesDisplay";

export default async function UserProfilePage({
    params,
}: {
    params: { slug: string[] };
}) {
    const slug = params.slug;
    const id = (slug[0] && Number(slug[0])) as number;
    const timelineId = (slug[1] && Number(slug[1])) as number;

    return (
        <div className="flex items-stretch">
            <TimelineSideBar authorId={id} activeTimelineId={timelineId} />
            <ArticleDisplay timelineId={timelineId} />
        </div>
    );
}
