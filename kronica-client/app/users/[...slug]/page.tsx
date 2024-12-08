import TimelineSideBar from "../TimelineSideBar";
import ArticleDisplay from "../ArticlesDisplay";
import UserProfileSection from "../UserProfileSection";

export default async function UserProfilePage({
    params,
}: {
    params: { slug: string[] };
}) {
    const slug = params.slug;
    const id = (slug[0] && Number(slug[0])) as number;
    const timelineId = (slug[1] && Number(slug[1])) as number;

    return (
        <div className="grid md:grid-cols-5 gap-5 bg-black">
            <div className="md:col-span-1  "></div>
            <div className="md:col-span-4 bg-white">
                <UserProfileSection userId={id} />
            </div>
            <div className="bg-white md:col-span-1">
                <TimelineSideBar authorId={id} activeTimelineId={timelineId} />
            </div>
            <div className="bg-white md:col-span-4">
                <ArticleDisplay timelineId={timelineId} />
            </div>
        </div>
    );
}
