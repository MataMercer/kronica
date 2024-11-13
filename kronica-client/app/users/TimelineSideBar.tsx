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
            <div className="text-3xl   m-4">Timelines</div>
            <div className="flex flex-col">
                <Link
                    href={`/users/${authorId}`}
                    key={0}
                    className={`p-3 border-[1px] m-3 border-black text-2xl border-b-4 
                ${!!!activeTimelineId && "bg-blue-400 text-white ml-6 mr-0"}
                `}
                >
                    ALL
                </Link>
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
                ${active && "bg-blue-400 text-white ml-6 mr-0"}
                `}
        >
            {timeline.name.toUpperCase()}
        </Link>
    );
}
