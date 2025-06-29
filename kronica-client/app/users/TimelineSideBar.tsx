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
        <div className="flex flex-col   md:h-[100vh]">
            <div className="text-2xl   m-1">Timelines</div>
            <div className="flex md:flex-col">
                <Link
                    href={`/users/${authorId}`}
                    key={0}
                    className={`p-2 border-[1px] m-1 border-black text-xl border-b-4 
                ${!!!activeTimelineId && "bg-[#8a2be2] text-white font-bold"}
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
            className={`p-2 border-[1px] m-1 border-black text-xl border-b-4 
                ${active && "bg-[#8a2be2] text-white  font-bold"}
                `}
        >
            {timeline.name.toUpperCase()}
        </Link>
    );
}
