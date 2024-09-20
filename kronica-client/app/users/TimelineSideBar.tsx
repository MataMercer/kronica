import { fetchAllTimelines, Timeline } from "@/app/fetch/timelines";
import Link from "next/link";

type TimelineSideBarProps = {
    authorId: number;
    activeTimelineId: number;
};
export default async function TimelineSideBar({
    authorId,
    activeTimelineId,
}: TimelineSideBarProps) {
    const timelines = await fetchAllTimelines(authorId);
    return (
        <div className="flex flex-col  border-r-[20px] border-black h-[100vh]">
            <div className="text-3xl border-b-[1px] border-black m-4">
                Timelines
            </div>
            <div className="flex flex-col">
                <div className="m-3 ml-6 mr-0 p-3 text-2xl bg-black text-white border-y-[1px] border-black ">
                    All
                </div>
                {timelines ? (
                    timelines.map((timeline) => (
                        <TimelineButton
                            key={timeline.id}
                            timeline={timeline}
                            authorId={authorId}
                            active={timeline.id === activeTimelineId}
                        />
                    ))
                ) : (
                    <div>There are no articles.</div>
                )}
            </div>
        </div>
    );
}

type TimelineButtonProps = {
    timeline: Timeline;
    authorId: number;
    active: boolean;
};
function TimelineButton({ timeline, authorId, active }: TimelineButtonProps) {
    return (
        <Link
            href={`/users/${authorId}/${timeline.id}`}
            key={timeline.id}
            className={`p-3 border-[1px] m-3 border-black text-2xl border-b-4 
                ${active && "bg-black text-white ml-6 mr-0"}
                `}
        >
            {timeline.name}
        </Link>
    );
}
