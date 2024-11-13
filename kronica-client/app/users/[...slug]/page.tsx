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
        <div className="grid grid-cols-5">
            <div className="col-span-1 border-b-[20px] border-black "></div>
            <div className="col-span-4">
                <UserProfileSection userId={id} />
            </div>
            <div>
                <TimelineSideBar authorId={id} activeTimelineId={timelineId} />
            </div>
            <div className="col-span-4">
                <ArticleDisplay timelineId={timelineId} />
            </div>
        </div>
    );
}
